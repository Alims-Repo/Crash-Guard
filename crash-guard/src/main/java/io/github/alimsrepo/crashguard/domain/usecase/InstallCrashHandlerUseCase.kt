package io.github.alimsrepo.crashguard.domain.usecase

import io.github.alimsrepo.crashguard.domain.handler.CrashExceptionHandler
import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.domain.repository.CrashRepository

/**
 * Use case for installing crash handler
 */
class InstallCrashHandlerUseCase(
    private val handler: CrashExceptionHandler
) {
    fun execute() {
        Thread.setDefaultUncaughtExceptionHandler(handler)
    }
}