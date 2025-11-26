package io.github.alimsrepo.crashguard.domain.usecase

import io.github.alimsrepo.crashguard.domain.repository.CrashRepository

/**
 * Use case for getting crash count
 */
class GetCrashCountUseCase(
    private val repository: CrashRepository
) {
    suspend fun execute(): Result<Int> {
        return repository.getCrashCount()
    }
}