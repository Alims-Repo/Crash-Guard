package io.github.alimsrepo.crashguard.domain.model

import android.os.Build
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Domain model representing crash data.
 * Exception info (type, message, stack trace) is stored eagerly as strings so that
 * the model is safely serializable even when the original Throwable is not.
 */
data class CrashData(
    val id: String = UUID.randomUUID().toString(),
    /** Retained in-memory only; excluded from Java serialization via @Transient. */
    @Transient val exception: Throwable? = null,
    val exceptionType: String,
    val exceptionMessage: String,
    val stackTrace: String,
    val threadName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val appVersion: String,
    val appPackage: String,
    val deviceInfo: DeviceInfo,
    val customData: Map<String, String> = emptyMap(),
    val activityStack: List<String> = emptyList(),
    val availableMemory: Long = 0L,
    val totalMemory: Long = 0L,
    val diskSpace: Long = 0L,
    val batteryLevel: Float = 0f,
    val isCharging: Boolean = false,
    val networkType: String = "Unknown",
    val orientation: String = "Unknown"
) : Serializable {

    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val shortTimestamp: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    fun getFullReport(): String = buildString {
        appendLine("==================== CRASH REPORT ====================")
        appendLine()
        appendLine("Crash ID: $id")
        appendLine("Timestamp: $formattedTimestamp")
        appendLine()
        appendLine("--- Application Info ---")
        appendLine("Package: $appPackage")
        appendLine("Version: $appVersion")
        appendLine("Thread: $threadName")
        appendLine()
        appendLine("--- Device Info ---")
        appendLine(deviceInfo.toFormattedString())
        appendLine()
        appendLine("--- System Info ---")
        appendLine("Available Memory: ${availableMemory / 1024 / 1024} MB")
        appendLine("Total Memory: ${totalMemory / 1024 / 1024} MB")
        appendLine("Disk Space: ${diskSpace / 1024 / 1024} MB")
        appendLine("Battery Level: ${(batteryLevel * 100).toInt()}%")
        appendLine("Charging: ${if (isCharging) "Yes" else "No"}")
        appendLine("Network: $networkType")
        appendLine("Orientation: $orientation")
        appendLine()

        if (activityStack.isNotEmpty()) {
            appendLine("--- Activity Stack ---")
            activityStack.forEachIndexed { index, activity ->
                appendLine("$index. $activity")
            }
            appendLine()
        }

        if (customData.isNotEmpty()) {
            appendLine("--- Custom Data ---")
            customData.forEach { (key, value) ->
                appendLine("$key: $value")
            }
            appendLine()
        }

        appendLine("--- Exception ---")
        appendLine("Type: $exceptionType")
        appendLine("Message: $exceptionMessage")
        appendLine()
        appendLine("--- Stack Trace ---")
        appendLine(stackTrace)
        appendLine("====================================================")
    }

    fun toJson(): String {
        fun String.escapeJson() = replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val customDataJson = customData.entries.joinToString(",\n") { (k, v) ->
            "  \"${k.escapeJson()}\": \"${v.escapeJson()}\""
        }
        val activityStackJson = activityStack.joinToString(", ") { "\"${it.escapeJson()}\"" }

        return """
            {
                "id": "$id",
                "timestamp": $timestamp,
                "exceptionType": "${exceptionType.escapeJson()}",
                "exceptionMessage": "${exceptionMessage.escapeJson()}",
                "stackTrace": "${stackTrace.escapeJson()}",
                "threadName": "${threadName.escapeJson()}",
                "appVersion": "${appVersion.escapeJson()}",
                "appPackage": "${appPackage.escapeJson()}",
                "availableMemory": $availableMemory,
                "totalMemory": $totalMemory,
                "diskSpace": $diskSpace,
                "batteryLevel": $batteryLevel,
                "isCharging": $isCharging,
                "networkType": "${networkType.escapeJson()}",
                "orientation": "${orientation.escapeJson()}",
                "activityStack": [$activityStackJson],
                "customData": {
$customDataJson
                },
                "deviceInfo": ${deviceInfo.toJson()}
            }
        """.trimIndent()
    }

    companion object {
        /** Helper to convert a Throwable to a stack-trace string safely. */
        fun stackTraceOf(throwable: Throwable): String {
            return try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                sw.toString()
            } catch (e: Exception) {
                buildString {
                    appendLine("Exception: ${throwable.javaClass.name}")
                    appendLine("Message: ${throwable.message ?: "No message"}")
                    appendLine()
                    appendLine("Stack trace generation failed: ${e.message}")
                    throwable.stackTrace?.forEach { appendLine("  at $it") }
                }
            }
        }
    }
}