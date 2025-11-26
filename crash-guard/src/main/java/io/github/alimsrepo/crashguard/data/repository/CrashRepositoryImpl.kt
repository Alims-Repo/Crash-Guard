package io.github.alimsrepo.crashguard.data.repository

import io.github.alimsrepo.crashguard.domain.model.CrashData
import io.github.alimsrepo.crashguard.domain.repository.CrashRepository
import io.github.alimsrepo.crashguard.data.storage.CrashLogStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of CrashRepository (Data Layer)
 */
class CrashRepositoryImpl(
    private val storage: CrashLogStorage
) : CrashRepository {

    override suspend fun saveCrash(crashData: CrashData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            storage.saveCrash(crashData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllCrashes(): Result<List<CrashData>> = withContext(Dispatchers.IO) {
        try {
            val crashes = storage.getAllCrashes()
            Result.success(crashes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCrashById(id: String): Result<CrashData?> = withContext(Dispatchers.IO) {
        try {
            val crash = storage.getCrashById(id)
            Result.success(crash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCrash(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            storage.deleteCrash(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllCrashes(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            storage.deleteAllCrashes()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCrashCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val count = storage.getCrashCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLastCrash(): Result<CrashData?> {
        return try {
            val crash = storage.getLastCrash()
            Result.success(crash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}