package io.github.alimsrepo.crashguard.domain.usecase

import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.domain.repository.CrashRepository

/**
 * Use case for retrieving all crashes
 */
class GetAllCrashesUseCase(
    private val repository: CrashRepository
) {
    suspend fun execute(): Result<List<CrashData>> {
        return repository.getAllCrashes()
    }
}