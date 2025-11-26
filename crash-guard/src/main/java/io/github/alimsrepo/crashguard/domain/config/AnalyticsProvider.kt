package io.github.alimsrepo.crashguard.domain.config

import io.github.alimsrepo.crashguard.domain.model.CrashData

/**
 * Analytics provider interface
 */
interface AnalyticsProvider {
    fun logCrash(crashData: CrashData)

    fun logEvent(eventName: String, params: Map<String, Any>)
}