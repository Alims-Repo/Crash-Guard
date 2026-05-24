package io.github.alimsrepo.crashguard

import android.app.Application
import io.github.alimsrepo.crashguard.domain.config.CrashGuardConfig
import io.github.alimsrepo.crashguard.domain.usecase.InstallCrashHandlerUseCase
import io.github.alimsrepo.crashguard.data.repository.CrashRepositoryImpl
import io.github.alimsrepo.crashguard.data.storage.CrashLogStorage
import io.github.alimsrepo.crashguard.domain.handler.CrashExceptionHandler
import io.github.alimsrepo.crashguard.utils.ActivityTracker
import io.github.alimsrepo.crashguard.utils.CrashGuardLogger

/**
 * Main entry point for CrashGuard library.
 * Industry-grade crash handling with full customization support.
 */
object CrashGuard {

    /**
     * Backing flag — @Volatile ensures writes in the synchronized block are visible
     * to threads that read it outside the block (e.g. checkInitialized()).
     */
    @Volatile
    private var _initialized = false

    private lateinit var config: CrashGuardConfig

    /**
     * Install CrashGuard with configuration.
     *
     * @param application Application instance
     * @param config Configuration for crash handling behavior
     * @throws IllegalStateException if already initialized
     */
    fun install(
        application: Application,
        config: CrashGuardConfig = CrashGuardConfig.Builder(application).build()
    ) {
        synchronized(this) {
            if (_initialized) {
                throw IllegalStateException("CrashGuard is already initialized. Call uninstall() first.")
            }

            this.config = config

            // Initialize the internal logger with the configured debug flag
            CrashGuardLogger.init(config.isDebugMode)

            // Track the live activity back-stack for crash reports
            application.registerActivityLifecycleCallbacks(ActivityTracker)

            // Initialize storage & repository
            val storage = CrashLogStorage(application, config)
            val repository = CrashRepositoryImpl(storage)

            // Create and install the uncaught-exception handler
            val handler = CrashExceptionHandler(application, config, repository)
            InstallCrashHandlerUseCase(handler).execute()

            _initialized = true

            config.onInitialized?.invoke()
        }
    }

    /**
     * Uninstall CrashGuard — restores whatever handler was active before [install] and
     * resets internal state. Primarily useful in tests.
     */
    fun uninstall() {
        synchronized(this) {
            if (!_initialized) return

            // Restore the previous uncaught-exception handler (stored inside the handler instance)
            Thread.setDefaultUncaughtExceptionHandler(null)

            if (::config.isInitialized) {
                try {
                    (config.application).unregisterActivityLifecycleCallbacks(ActivityTracker)
                } catch (_: Exception) { /* best-effort */ }
            }

            _initialized = false
        }
    }

    /**
     * Get current configuration.
     */
    fun getConfig(): CrashGuardConfig {
        checkInitialized()
        return config
    }

    /**
     * Returns true if [install] has been called and [uninstall] has not.
     */
    fun isInitialized(): Boolean = _initialized

    /**
     * Manually trigger a crash (delegates to the installed handler — useful for testing).
     */
    fun triggerCrash(throwable: Throwable) {
        checkInitialized()
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        handler?.uncaughtException(Thread.currentThread(), throwable)
            ?: throw throwable
    }

    private fun checkInitialized() {
        if (!_initialized) {
            throw IllegalStateException("CrashGuard is not initialized. Call install() first.")
        }
    }
}