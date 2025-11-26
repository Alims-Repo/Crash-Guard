package io.github.alimsrepo.crashguard.domain.usecase

import io.github.alimsrepo.crashguard.domain.repository.CrashRepository

/**
 * Use case for deleting a crash
 */
class DeleteCrashUseCase(
    private val repository: CrashRepository
) {
    suspend fun execute(id: String): Result<Unit> {
        return repository.deleteCrash(id)
    }
}