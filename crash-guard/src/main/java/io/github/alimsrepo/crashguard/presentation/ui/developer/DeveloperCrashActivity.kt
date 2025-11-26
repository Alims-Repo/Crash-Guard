package io.github.alimsrepo.crashguard.presentation.ui.developer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.github.alimsrepo.crashguard.R
import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.presentation.ui.base.BaseCrashActivity
import io.github.alimsrepo.crashguard.presentation.ui.base.CrashActivityContract

/**
 * Developer crash screen for debug builds
 * Shows detailed crash information with full stack trace
 */
class DeveloperCrashActivity : BaseCrashActivity(), CrashActivityContract {

    private lateinit var tvExceptionType: TextView
    private lateinit var tvExceptionMessage: TextView
    private lateinit var tvDeviceInfo: TextView
    private lateinit var tvStackTrace: TextView
    private lateinit var tvMemoryInfo: TextView
    private lateinit var tvCustomData: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnShare: Button
    private lateinit var btnRestart: Button
    private lateinit var btnClose: Button

    override fun setupUI() {
        setContentView(R.layout.activity_developer_crash)

        tvExceptionType = findViewById(R.id.tv_exception_type)
        tvExceptionMessage = findViewById(R.id.tv_exception_message)
        tvDeviceInfo = findViewById(R.id.tv_device_info)
        tvStackTrace = findViewById(R.id.tv_stack_trace)
        tvMemoryInfo = findViewById(R.id.tv_memory_info)
        tvCustomData = findViewById(R.id.tv_custom_data)
        btnCopy = findViewById(R.id.btn_copy_crash)
        btnShare = findViewById(R.id.btn_share_crash)
        btnRestart = findViewById(R.id.btn_restart_dev)
        btnClose = findViewById(R.id.btn_close_dev)

        crashData?.let { onCrashDataReceived(it) }

        btnCopy.setOnClickListener { copyCrashReport() }
        btnShare.setOnClickListener { shareCrashReport() }
        btnRestart.setOnClickListener { onRestartRequested() }
        btnClose.setOnClickListener { onCloseRequested() }
    }

    override fun onCrashDataReceived(crashData: CrashData) {
        tvExceptionType.text = crashData.exceptionType
        tvExceptionMessage.text = crashData.exceptionMessage
        tvDeviceInfo.text = crashData.deviceInfo.toFormattedString()

        // Safely get stack trace
        tvStackTrace.text = try {
            crashData.stackTrace
        } catch (e: Exception) {
            "Stack trace unavailable: ${e.message}"
        }

        // Memory info
        val memoryInfo = buildString {
            appendLine("Available: ${crashData.availableMemory / 1024 / 1024} MB")
            appendLine("Total: ${crashData.totalMemory / 1024 / 1024} MB")
            appendLine("Battery: ${(crashData.batteryLevel * 100).toInt()}%")
            appendLine("Network: ${crashData.networkType}")
        }
        tvMemoryInfo.text = memoryInfo

        // Custom data
        if (crashData.customData.isNotEmpty()) {
            val customDataText = buildString {
                crashData.customData.forEach { (key, value) ->
                    appendLine("$key: $value")
                }
            }
            tvCustomData.text = customDataText
        } else {
            tvCustomData.text = "No custom data"
        }
    }

    override fun onRestartRequested() {
        restartApp()
    }

    override fun onCloseRequested() {
        closeApp()
    }
}