package io.github.alimsrepo.crashguard.domain.usecase

import io.github.alimsrepo.crashguard.domain.repository.CrashRepository

/**
 * Use case for deleting all crashes
 */
class DeleteAllCrashesUseCase(
    private val repository: CrashRepository
) {
    suspend fun execute(): Result<Unit> {
        return repository.deleteAllCrashes()
    }
}