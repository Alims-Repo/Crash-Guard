package io.github.alimsrepo.crashguard.domain.model

import android.os.Build
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Domain model representing crash data
 */
data class CrashData(
    val id: String = UUID.randomUUID().toString(),
    val exception: Throwable,
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

    val exceptionType: String
        get() = exception.javaClass.name

    val exceptionMessage: String
        get() = exception.message ?: "No message"

    val stackTrace: String
        get() {
            return try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                exception.printStackTrace(pw)
                pw.flush()
                sw.toString()
            } catch (e: Exception) {
                // Fallback if printStackTrace fails
                buildString {
                    appendLine("Exception: ${exception.javaClass.name}")
                    appendLine("Message: ${exception.message ?: "No message"}")
                    appendLine()
                    appendLine("Stack trace generation failed: ${e.message}")
                    appendLine()
                    appendLine("Basic stack trace:")
                    exception.stackTrace?.forEach { element ->
                        appendLine("  at $element")
                    }
                }
            }
        }

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
        // Simple JSON serialization (can be replaced with Gson/Moshi)
        return """
            {
                "id": "$id",
                "timestamp": $timestamp,
                "exceptionType": "$exceptionType",
                "exceptionMessage": "${exceptionMessage.replace("\"", "\\\"")}",
                "threadName": "$threadName",
                "appVersion": "$appVersion",
                "appPackage": "$appPackage",
                "deviceInfo": ${deviceInfo.toJson()},
                "availableMemory": $availableMemory,
                "totalMemory": $totalMemory,
                "batteryLevel": $batteryLevel,
                "isCharging": $isCharging,
                "networkType": "$networkType"
            }
        """.trimIndent()
    }
}