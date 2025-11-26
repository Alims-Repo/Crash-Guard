package io.github.alimsrepo.crashguard.presentation.ui.base

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.alimsrepo.crashguard.CrashGuard
import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.utils.CrashGuardConstants

/**
 * Base activity for crash screens
 * Provides common functionality for both user and developer screens
 */
abstract class BaseCrashActivity : AppCompatActivity() {

    protected var crashData: CrashData? = null
    protected val config by lazy { CrashGuard.getConfig() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve crash data
        crashData = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                CrashGuardConstants.EXTRA_CRASH_DATA,
                CrashData::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(CrashGuardConstants.EXTRA_CRASH_DATA) as? CrashData
        }

        if (crashData == null) {
            // If no crash data, close activity
            finish()
            return
        }

        setupUI()
    }

    /**
     * Setup UI - to be implemented by subclasses
     */
    protected abstract fun setupUI()

    /**
     * Restart the application
     */
    protected fun restartApp() {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (config.enableAutoRestart && config.restartDelayMs > 0) {
                    android.os.Handler(mainLooper).postDelayed({
                        startActivity(launchIntent)
                        finish()
                    }, config.restartDelayMs)
                } else {
                    startActivity(launchIntent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Unable to restart app", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    /**
     * Copy crash report to clipboard
     */
    protected fun copyCrashReport() {
        crashData?.let { data ->
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Crash Report", data.getFullReport())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Crash report copied", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to copy", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Share crash report
     */
    protected fun shareCrashReport() {
        crashData?.let { data ->
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "App Crash Report - ${data.id}")
                    putExtra(Intent.EXTRA_TEXT, data.getFullReport())
                }
                startActivity(Intent.createChooser(intent, "Share crash report"))
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to share", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Close the app
     */
    protected fun closeApp() {
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onBackPressed() {
        // Prevent going back to crashed state
        if (config.enableAutoRestart) {
            restartApp()
        } else {
            closeApp()
        }
    }
}