package io.github.alimsrepo.crashguard

import android.app.Application
import android.util.Log
import io.github.alimsrepo.crashguard.domain.config.CrashGuardConfig
import io.github.alimsrepo.crashguard.domain.usecase.InstallCrashHandlerUseCase
import io.github.alimsrepo.crashguard.data.repository.CrashRepositoryImpl
import io.github.alimsrepo.crashguard.data.storage.CrashLogStorage
import io.github.alimsrepo.crashguard.domain.handler.CrashExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main entry point for CrashGuard library
 * Industry-grade crash handling with full customization support
 */
object CrashGuard {

    private var isInitialized = false
    private lateinit var config: CrashGuardConfig

    /**
     * Install CrashGuard with configuration
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
            if (isInitialized) {
                throw IllegalStateException("CrashGuard is already initialized")
            }

            this.config = config

            // Initialize storage
            val storage = CrashLogStorage(application, config)

            // Initialize repository
            val repository = CrashRepositoryImpl(storage)

//            CoroutineScope(Dispatchers.IO).launch {
//                repository.getAllCrashes().let {
//                    it.getOrNull()?.forEach {
//                        Log.e("RAW", it.toString())
//                    }
//                }
//            }

            // Create and install exception handler
            val handler = CrashExceptionHandler(application, config, repository)
            val useCase = InstallCrashHandlerUseCase(handler)
            useCase.execute()

            isInitialized = true

            config.onInitialized?.invoke()
        }
    }

    /**
     * Get current configuration
     */
    fun getConfig(): CrashGuardConfig {
        checkInitialized()
        return config
    }

    /**
     * Check if CrashGuard is initialized
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Manually trigger crash handler (for testing)
     */
    fun triggerCrash(throwable: Throwable) {
        checkInitialized()
        throw throwable
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("CrashGuard is not initialized. Call install() first.")
        }
    }
}