package com.alim.crash.guard

import android.app.Application
import android.content.Context
import io.github.alimsrepo.crashguard.CrashGuard
import io.github.alimsrepo.crashguard.domain.config.AnalyticsProvider
import io.github.alimsrepo.crashguard.domain.config.CrashGuardConfig
import io.github.alimsrepo.crashguard.domain.config.CrashInterceptor
import io.github.alimsrepo.crashguard.domain.config.CustomDataProvider
import io.github.alimsrepo.crashguard.domain.config.NotificationConfig
import io.github.alimsrepo.crashguard.domain.model.CrashData

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Basic initialization
        CrashGuard.install(
            application = this,
            config = CrashGuardConfig.Builder(this)
                .debugMode(true)
                .build()
        )
    }
}