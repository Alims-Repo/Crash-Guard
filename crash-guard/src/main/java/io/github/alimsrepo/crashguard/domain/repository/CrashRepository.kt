
package io.github.alimsrepo.crashguard.domain.repository

import io.github.alimsrepo.crashguard.domain.model.CrashData

/**
 * Repository interface for crash data operations (Domain Layer)
 */
interface CrashRepository {
    suspend fun saveCrash(crashData: CrashData): Result<Unit>
    suspend fun getAllCrashes(): Result<List<CrashData>>
    suspend fun getCrashById(id: String): Result<CrashData?>
    suspend fun deleteCrash(id: String): Result<Unit>
    suspend fun deleteAllCrashes(): Result<Unit>
    suspend fun getCrashCount(): Result<Int>
    fun getLastCrash(): Result<CrashData?>
}
