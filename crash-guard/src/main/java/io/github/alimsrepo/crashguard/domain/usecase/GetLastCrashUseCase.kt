package io.github.alimsrepo.crashguard.domain.usecase

import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.domain.repository.CrashRepository

/**
 * Use case for getting last crash
 */
class GetLastCrashUseCase(
    private val repository: CrashRepository
) {
    suspend fun execute(): Result<CrashData?> {
        return repository.getLastCrash()
    }
}