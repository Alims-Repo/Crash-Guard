package io.github.alimsrepo.crashguard.domain.usecase

import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.domain.repository.CrashRepository

/**
 * Use case for retrieving a specific crash
 */
class GetCrashByIdUseCase(
    private val repository: CrashRepository
) {
    suspend fun execute(id: String): Result<CrashData?> {
        return repository.getCrashById(id)
    }
}