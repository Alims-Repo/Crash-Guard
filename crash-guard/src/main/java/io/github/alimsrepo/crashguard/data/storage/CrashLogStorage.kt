package io.github.alimsrepo.crashguard.data.storage

import android.content.Context
import io.github.alimsrepo.crashguard.domain.config.CrashGuardConfig
import io.github.alimsrepo.crashguard.domain.model.CrashData
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Handles crash log storage and retrieval
 */
class CrashLogStorage(
    private val context: Context,
    private val config: CrashGuardConfig
) {

    private val storageDir: File by lazy {
        val dir = if (config.logStoragePath != null) {
            File(config.logStoragePath)
        } else {
            File(context.filesDir, "crash_logs")
        }

        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    /**
     * Save crash data to storage
     */
    fun saveCrash(crashData: CrashData) {
        if (!config.enableLogging) return

        try {
            // Enforce max crash logs limit
            enforceMaxCrashLogs()

            val file = File(storageDir, "${crashData.id}.crash")
            FileOutputStream(file).use { fos ->
                ObjectOutputStream(fos).use { oos ->
                    oos.writeObject(crashData)
                }
            }

            // Also save as text for easy reading
            if (config.isDebugMode) {
                val textFile = File(storageDir, "${crashData.id}.txt")
                textFile.writeText(crashData.getFullReport())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get all crash logs
     */
    fun getAllCrashes(): List<CrashData> {
        return try {
            storageDir.listFiles { file -> file.extension == "crash" }
                ?.mapNotNull { file -> readCrashFile(file) }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get crash by ID
     */
    fun getCrashById(id: String): CrashData? {
        return try {
            val file = File(storageDir, "$id.crash")
            if (file.exists()) {
                readCrashFile(file)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete specific crash log
     */
    fun deleteCrash(id: String) {
        try {
            File(storageDir, "$id.crash").delete()
            File(storageDir, "$id.txt").delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete all crash logs (only .crash and .txt files — leaves other files untouched).
     */
    fun deleteAllCrashes() {
        try {
            storageDir.listFiles { file -> file.extension == "crash" || file.extension == "txt" }
                ?.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get crash count
     */
    fun getCrashCount(): Int {
        return try {
            storageDir.listFiles { file -> file.extension == "crash" }?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get last crash
     */
    fun getLastCrash(): CrashData? {
        return getAllCrashes().firstOrNull()
    }

    /**
     * Export all crashes as JSON
     */
    fun exportCrashesAsJson(): String {
        val crashes = getAllCrashes()
        return buildString {
            appendLine("[")
            crashes.forEachIndexed { index, crash ->
                appendLine(crash.toJson())
                if (index < crashes.size - 1) {
                    appendLine(",")
                }
            }
            appendLine("]")
        }
    }

    /**
     * Export specific crash as text file
     */
    fun exportCrashAsText(crashData: CrashData): File {
        val exportDir = File(context.getExternalFilesDir(null), "crash_exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, "crash_${crashData.id}.txt")
        file.writeText(crashData.getFullReport())
        return file
    }

    private fun readCrashFile(file: File): CrashData? {
        return try {
            FileInputStream(file).use { fis ->
                ObjectInputStream(fis).use { ois ->
                    ois.readObject() as CrashData
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun enforceMaxCrashLogs() {
        val files = storageDir.listFiles { file -> file.extension == "crash" }
            ?.sortedBy { it.lastModified() }
            ?: return

        // Delete enough old files so that after the new one is saved we stay within the limit.
        // Without the +1 offset, when files.size == maxCrashLogs nothing was deleted here,
        // then the new file was saved, leaving maxCrashLogs+1 files stored permanently.
        val excessCount = files.size - config.maxCrashLogs + 1
        if (excessCount > 0) {
            files.take(excessCount).forEach { file ->
                file.delete()
                File(storageDir, "${file.nameWithoutExtension}.txt").delete()
            }
        }
    }
}