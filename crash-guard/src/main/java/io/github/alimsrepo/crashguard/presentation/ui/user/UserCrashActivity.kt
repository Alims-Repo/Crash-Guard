package io.github.alimsrepo.crashguard.presentation.ui.user

import android.widget.Button
import android.widget.TextView
import io.github.alimsrepo.crashguard.R
import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.presentation.ui.base.BaseCrashActivity
import io.github.alimsrepo.crashguard.presentation.ui.base.CrashActivityContract

/**
 * User-friendly crash screen for release builds
 * Shows minimal information and provides restart/close options
 */
class UserCrashActivity : BaseCrashActivity(), CrashActivityContract {

    private lateinit var tvTitle: TextView
    private lateinit var tvMessage: TextView
    private lateinit var tvTimestamp: TextView
    private lateinit var btnRestart: Button
    private lateinit var btnClose: Button

    override fun setupUI() {
        setContentView(R.layout.activity_user_crash)

        tvTitle = findViewById(R.id.tv_crash_title)
        tvMessage = findViewById(R.id.tv_crash_message)
        tvTimestamp = findViewById(R.id.tv_crash_timestamp)
        btnRestart = findViewById(R.id.btn_restart)
        btnClose = findViewById(R.id.btn_close)

        crashData?.let { onCrashDataReceived(it) }

        btnRestart.setOnClickListener { onRestartRequested() }
        btnClose.setOnClickListener { onCloseRequested() }
    }

    override fun onCrashDataReceived(crashData: CrashData) {
        tvTitle.text = "Oops! Something went wrong"
        tvMessage.text = "We're sorry, but the app encountered an unexpected error and needs to close. " +
                "Your data has been saved and you can restart the app."
        tvTimestamp.text = "Error occurred at ${crashData.shortTimestamp}"
    }

    override fun onRestartRequested() {
        restartApp()
    }

    override fun onCloseRequested() {
        closeApp()
    }
}