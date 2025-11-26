package io.github.alimsrepo.crashguard.utils


/**
 * Extension functions for CrashGuard
 */
object CrashGuardExtensions {

    /**
     * Format bytes to human-readable string
     */
    fun Long.formatBytes(): String {
        val kb = this / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$this B"
        }
    }

    /**
     * Sanitize sensitive data from strings
     */
    fun String.sanitize(): String {
        return this
            .replace(Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), "[EMAIL]")
            .replace(Regex("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"), "[CARD]")
            .replace(Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"), "[SSN]")
            .replace(Regex("\\b\\d{10,}\\b"), "[PHONE]")
    }

    /**
     * Truncate long strings
     */
    fun String.truncate(maxLength: Int = 100): String {
        return if (this.length > maxLength) {
            "${this.substring(0, maxLength)}..."
        } else {
            this
        }
    }
}