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
import android.util.Log
import android.view.WindowManager
import io.github.alimsrepo.crashguard.domain.config.CrashGuardConfig
import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.domain.model.DeviceInfo
import io.github.alimsrepo.crashguard.domain.repository.CrashRepository
import io.github.alimsrepo.crashguard.utils.CrashGuardConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

/**
 * Handles uncaught exceptions
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

            // Intercept crash if interceptor is configured
            if (config.crashInterceptor?.onCrashIntercepted(throwable, thread) == true) {
                return
            }

            // Collect crash data
            val crashData = collectCrashData(throwable, thread)

            // Save crash data
            saveCrashData(crashData)

            // Trigger callbacks
            config.onCrashDetected?.invoke(crashData)

            // Check if crash screen should be shown
            val shouldShowScreen = config.onBeforeCrashScreen?.invoke(crashData) ?: true

            if (shouldShowScreen) {
                // Launch crash activity
                launchCrashActivity(crashData)
            }

            // Analytics
            if (config.enableAnalytics && config.analyticsProvider != null) {
                try {
                    config.analyticsProvider.logCrash(crashData)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Terminate process
            terminateProcess()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun shouldExcludeException(throwable: Throwable): Boolean {
        return config.excludedExceptions.any { it.java.isInstance(throwable) }
    }

    private fun collectCrashData(throwable: Throwable, thread: Thread): CrashData {
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Get device info with screen details
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val deviceInfo = DeviceInfo(
            screenDensity = "${displayMetrics.densityDpi} dpi",
            screenResolution = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        )

        // Get memory info
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        // Get battery info
        val batteryIntent = application.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level / scale.toFloat()
        } ?: 0f

        val isCharging = batteryIntent?.let {
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } ?: false

        // Get network info
        val networkType = getNetworkType()

        // Get activity stack
        val activityStack = getActivityStack()

        // Get custom data
        val customData = config.customDataProvider?.provideCustomData(application) ?: emptyMap()

        // Get orientation
        val orientation = when (application.resources.configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> "Portrait"
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
            else -> "Unknown"
        }

        return CrashData(
            exception = throwable,
            threadName = thread.name,
            appVersion = packageInfo.versionName ?: "Unknown",
            appPackage = application.packageName,
            deviceInfo = deviceInfo,
            customData = customData,
            activityStack = activityStack,
            availableMemory = memoryInfo.availMem,
            totalMemory = memoryInfo.totalMem,
            diskSpace = application.filesDir.freeSpace,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            networkType = networkType,
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
            val networkInfo = cm.activeNetworkInfo
            networkInfo?.typeName ?: "No Connection"
        }
    }

    private fun getActivityStack(): List<String> {
        return try {
            val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            val tasks = activityManager.getRunningTasks(10)
            tasks.map { it.topActivity?.className ?: "Unknown" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCrashData(crashData: CrashData) {
        if (!config.enableLogging) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.saveCrash(crashData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun launchCrashActivity(crashData: CrashData) {
        try {
            val activityClass = config.getActivityClass()
            val intent = Intent(application, activityClass.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(CrashGuardConstants.EXTRA_CRASH_DATA, crashData)
            }
            application.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun terminateProcess() {
        config.onAfterCrashScreen?.invoke(
            repository.getLastCrash().getOrNull() ?: return
        )

        Process.killProcess(Process.myPid())
        exitProcess(1)
    }
}