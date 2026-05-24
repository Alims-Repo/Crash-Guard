package io.github.alimsrepo.crashguard.domain.handler

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Process
import android.util.DisplayMetrics
import android.view.WindowManager
import io.github.alimsrepo.crashguard.domain.config.CrashGuardConfig
import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.domain.model.DeviceInfo
import io.github.alimsrepo.crashguard.domain.repository.CrashRepository
import io.github.alimsrepo.crashguard.utils.ActivityTracker
import io.github.alimsrepo.crashguard.utils.CrashGuardConstants
import io.github.alimsrepo.crashguard.utils.CrashGuardLogger
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

/**
 * Handles uncaught exceptions.
 */
class CrashExceptionHandler(
    private val application: Application,
    private val config: CrashGuardConfig,
    private val repository: CrashRepository
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Check if exception should be excluded
            if (shouldExcludeException(throwable)) {
                defaultHandler?.uncaughtException(thread, throwable)
                return
            }

            // Intercept crash — if interceptor returns true it has handled the crash,
            // but we still forward to the default handler so the system is aware.
            if (config.crashInterceptor?.onCrashIntercepted(throwable, thread) == true) {
                defaultHandler?.uncaughtException(thread, throwable)
                return
            }

            // Collect crash data
            val crashData = collectCrashData(throwable, thread)

            // Save crash data synchronously — must complete before we kill the process
            saveCrashDataSync(crashData)

            // Trigger callbacks
            config.onCrashDetected?.invoke(crashData)

            // Check if crash screen should be shown
            val shouldShowScreen = config.onBeforeCrashScreen?.invoke(crashData) ?: true
            if (shouldShowScreen) {
                launchCrashActivity(crashData)
            }

            // Analytics
            if (config.enableAnalytics && config.analyticsProvider != null) {
                try {
                    config.analyticsProvider.logCrash(crashData)
                } catch (e: Exception) {
                    CrashGuardLogger.e("Handler", "Analytics error", e)
                }
            }

            // Terminate process — pass crashData directly so onAfterCrashScreen always fires
            terminateProcess(crashData)

        } catch (e: Exception) {
            CrashGuardLogger.e("Handler", "Error in uncaughtException handler", e)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun shouldExcludeException(throwable: Throwable): Boolean {
        return config.excludedExceptions.any { it.java.isInstance(throwable) }
    }

    private fun collectCrashData(throwable: Throwable, thread: Thread): CrashData {
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Resolve screen metrics using the non-deprecated API on API 30+
        val (screenDensity, screenResolution) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            val densityDpi = application.resources.displayMetrics.densityDpi
            "${densityDpi} dpi" to "${bounds.width()}x${bounds.height()}"
        } else {
            @Suppress("DEPRECATION")
            val dm = DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }
            "${dm.densityDpi} dpi" to "${dm.widthPixels}x${dm.heightPixels}"
        }

        val deviceInfo = DeviceInfo(
            screenDensity = screenDensity,
            screenResolution = screenResolution
        )

        // Memory
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        // Battery
        val batteryIntent = application.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val batteryLevel = batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (scale > 0) level / scale.toFloat() else 0f
        } ?: 0f
        val isCharging = batteryIntent?.let {
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } ?: false

        val orientation = when (application.resources.configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> "Portrait"
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
            else -> "Unknown"
        }

        return CrashData(
            exception = throwable,
            exceptionType = throwable.javaClass.name,
            exceptionMessage = throwable.message ?: "No message",
            stackTrace = CrashData.stackTraceOf(throwable),
            threadName = thread.name,
            appVersion = packageInfo.versionName ?: "Unknown",
            appPackage = application.packageName,
            deviceInfo = deviceInfo,
            customData = config.customDataProvider?.provideCustomData(application) ?: emptyMap(),
            activityStack = ActivityTracker.getStack(),
            availableMemory = memoryInfo.availMem,
            totalMemory = memoryInfo.totalMem,
            diskSpace = application.filesDir.freeSpace,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            networkType = getNetworkType(),
            orientation = orientation
        )
    }

    private fun getNetworkType(): String {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return "No Connection"
            val capabilities = cm.getNetworkCapabilities(network) ?: return "Unknown"
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Unknown"
            }
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.typeName ?: "No Connection"
        }
    }

    /**
     * Saves crash data synchronously using runBlocking.
     * We must block here — the process is killed immediately after this call, so a
     * fire-and-forget coroutine would never complete.
     */
    private fun saveCrashDataSync(crashData: CrashData) {
        if (!config.enableLogging) return
        try {
            runBlocking {
                repository.saveCrash(crashData)
            }
        } catch (e: Exception) {
            CrashGuardLogger.e("Handler", "Failed to save crash data", e)
        }
    }

    private fun launchCrashActivity(crashData: CrashData) {
        try {
            val intent = Intent(application, config.getActivityClass().java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(CrashGuardConstants.EXTRA_CRASH_DATA, crashData)
            }
            application.startActivity(intent)
        } catch (e: Exception) {
            CrashGuardLogger.e("Handler", "Failed to launch crash activity", e)
        }
    }

    /**
     * Invokes [CrashGuardConfig.onAfterCrashScreen] with the current crash data, then
     * terminates the process unconditionally.
     *
     * Previously this read `getLastCrash()` from storage (which was always empty because
     * the async save had not yet completed) and could silently skip `killProcess()` via
     * `?: return`, leaving the app as a zombie.
     */
    private fun terminateProcess(crashData: CrashData) {
        try {
            config.onAfterCrashScreen?.invoke(crashData)
        } catch (e: Exception) {
            CrashGuardLogger.e("Handler", "Error in onAfterCrashScreen callback", e)
        }

        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}