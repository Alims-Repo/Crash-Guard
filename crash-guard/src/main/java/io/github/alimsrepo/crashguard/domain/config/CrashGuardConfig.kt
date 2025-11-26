package io.github.alimsrepo.crashguard.domain.config

import android.app.Application
import android.content.Context
import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.presentation.ui.developer.DeveloperCrashActivity
import io.github.alimsrepo.crashguard.presentation.ui.user.UserCrashActivity
import kotlin.reflect.KClass

/**
 * Configuration class for CrashGuard
 * Provides extensive customization options
 */
@ConsistentCopyVisibility
data class CrashGuardConfig internal constructor(
    val application: Application,
    val isDebugMode: Boolean,
    val enableLogging: Boolean,
    val enableCrashReporting: Boolean,
    val maxCrashLogs: Int,
    val customUserActivity: KClass<*>?,
    val customDeveloperActivity: KClass<*>?,
    val crashInterceptor: CrashInterceptor?,
    val logStoragePath: String?,
    val enableAutoRestart: Boolean,
    val restartDelayMs: Long,
    val showNotification: Boolean,
    val notificationConfig: NotificationConfig?,
    val enableAnalytics: Boolean,
    val analyticsProvider: AnalyticsProvider?,
    val onCrashDetected: ((CrashData) -> Unit)?,
    val onBeforeCrashScreen: ((CrashData) -> Boolean)?,
    val onAfterCrashScreen: ((CrashData) -> Unit)?,
    val onInitialized: (() -> Unit)?,
    val customDataProvider: CustomDataProvider?,
    val enableSecureMode: Boolean,
    val excludedExceptions: Set<KClass<out Throwable>>
) {

    /**
     * Builder for CrashGuardConfig with fluent API
     */
    class Builder(private val application: Application) {
        private var isDebugMode: Boolean = false
        private var enableLogging: Boolean = true
        private var enableCrashReporting: Boolean = true
        private var maxCrashLogs: Int = 50
        private var customUserActivity: KClass<*>? = null
        private var customDeveloperActivity: KClass<*>? = null
        private var crashInterceptor: CrashInterceptor? = null
        private var logStoragePath: String? = null
        private var enableAutoRestart: Boolean = false
        private var restartDelayMs: Long = 1000L
        private var showNotification: Boolean = false
        private var notificationConfig: NotificationConfig? = null
        private var enableAnalytics: Boolean = false
        private var analyticsProvider: AnalyticsProvider? = null
        private var onCrashDetected: ((CrashData) -> Unit)? = null
        private var onBeforeCrashScreen: ((CrashData) -> Boolean)? = null
        private var onAfterCrashScreen: ((CrashData) -> Unit)? = null
        private var onInitialized: (() -> Unit)? = null
        private var customDataProvider: CustomDataProvider? = null
        private var enableSecureMode: Boolean = false
        private var excludedExceptions: MutableSet<KClass<out Throwable>> = mutableSetOf()

        /**
         * Enable debug mode (shows developer crash screen)
         */
        fun debugMode(enabled: Boolean) = apply { this.isDebugMode = enabled }

        /**
         * Enable crash log storage
         */
        fun enableLogging(enabled: Boolean) = apply { this.enableLogging = enabled }

        /**
         * Enable crash reporting
         */
        fun enableCrashReporting(enabled: Boolean) = apply { this.enableCrashReporting = enabled }

        /**
         * Set maximum number of crash logs to store
         */
        fun maxCrashLogs(max: Int) = apply { this.maxCrashLogs = max }

        /**
         * Set custom user crash activity
         */
        fun customUserActivity(activity: KClass<*>) = apply { this.customUserActivity = activity }

        /**
         * Set custom developer crash activity
         */
        fun customDeveloperActivity(activity: KClass<*>) = apply { this.customDeveloperActivity = activity }

        /**
         * Set crash interceptor for pre-processing
         */
        fun crashInterceptor(interceptor: CrashInterceptor) = apply { this.crashInterceptor = interceptor }

        /**
         * Set custom log storage path
         */
        fun logStoragePath(path: String) = apply { this.logStoragePath = path }

        /**
         * Enable automatic app restart after crash
         */
        fun enableAutoRestart(enabled: Boolean, delayMs: Long = 1000L) = apply {
            this.enableAutoRestart = enabled
            this.restartDelayMs = delayMs
        }

        /**
         * Show notification when app crashes
         */
        fun showNotification(enabled: Boolean, config: NotificationConfig? = null) = apply {
            this.showNotification = enabled
            this.notificationConfig = config
        }

        /**
         * Enable analytics integration
         */
        fun enableAnalytics(enabled: Boolean, provider: AnalyticsProvider? = null) = apply {
            this.enableAnalytics = enabled
            this.analyticsProvider = provider
        }

        /**
         * Callback when crash is detected
         */
        fun onCrashDetected(callback: (CrashData) -> Unit) = apply { this.onCrashDetected = callback }

        /**
         * Callback before showing crash screen
         * Return false to prevent showing crash screen
         */
        fun onBeforeCrashScreen(callback: (CrashData) -> Boolean) = apply { this.onBeforeCrashScreen = callback }

        /**
         * Callback after crash screen is dismissed
         */
        fun onAfterCrashScreen(callback: (CrashData) -> Unit) = apply { this.onAfterCrashScreen = callback }

        /**
         * Callback when CrashGuard is initialized
         */
        fun onInitialized(callback: () -> Unit) = apply { this.onInitialized = callback }

        /**
         * Set custom data provider for additional crash context
         */
        fun customDataProvider(provider: CustomDataProvider) = apply { this.customDataProvider = provider }

        /**
         * Enable secure mode (sanitizes sensitive data)
         */
        fun enableSecureMode(enabled: Boolean) = apply { this.enableSecureMode = enabled }

        /**
         * Exclude specific exception types from being handled
         */
        fun excludeException(exceptionClass: KClass<out Throwable>) = apply {
            this.excludedExceptions.add(exceptionClass)
        }

        /**
         * Build the configuration
         */
        fun build(): CrashGuardConfig {
            return CrashGuardConfig(
                application = application,
                isDebugMode = isDebugMode,
                enableLogging = enableLogging,
                enableCrashReporting = enableCrashReporting,
                maxCrashLogs = maxCrashLogs,
                customUserActivity = customUserActivity,
                customDeveloperActivity = customDeveloperActivity,
                crashInterceptor = crashInterceptor,
                logStoragePath = logStoragePath,
                enableAutoRestart = enableAutoRestart,
                restartDelayMs = restartDelayMs,
                showNotification = showNotification,
                notificationConfig = notificationConfig,
                enableAnalytics = enableAnalytics,
                analyticsProvider = analyticsProvider,
                onCrashDetected = onCrashDetected,
                onBeforeCrashScreen = onBeforeCrashScreen,
                onAfterCrashScreen = onAfterCrashScreen,
                onInitialized = onInitialized,
                customDataProvider = customDataProvider,
                enableSecureMode = enableSecureMode,
                excludedExceptions = excludedExceptions
            )
        }
    }

    fun getUserActivityClass() = customUserActivity ?: UserCrashActivity::class
    fun getDeveloperActivityClass() = customDeveloperActivity ?: DeveloperCrashActivity::class

    fun getActivityClass() = if (isDebugMode) getDeveloperActivityClass() else getUserActivityClass()
}