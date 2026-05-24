package com.alim.crash.guard

import android.app.Application
import io.github.alimsrepo.crashguard.CrashGuard
import io.github.alimsrepo.crashguard.domain.config.CrashGuardConfig

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Basic initialization
        CrashGuard.install(
            application = this,
            config = CrashGuardConfig.Builder(this)
                .debugMode(true)
                .customUserActivity(MainActivity::class)
                .build()
        )
    }
}