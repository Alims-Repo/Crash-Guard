package io.github.alimsrepo.crashguard.utils

/**
 * Extension functions for CrashGuard.
 *
 * Previously these were declared as member-extensions inside a CrashGuardExtensions object,
 * which made them inaccessible as extensions anywhere outside that object. They are now
 * top-level functions, available across the entire library and to consumers.
 */

/**
 * Format bytes to a human-readable string.
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
 * Sanitize common sensitive patterns from strings.
 *
 * Note: the phone pattern was originally `\b\d{10,}\b`, which matched any 10+ digit
 * sequence including Unix timestamps and memory addresses. It is now restricted to the
 * typical E.164 / NANP formats to avoid false positives.
 */
fun String.sanitize(): String {
    return this
        .replace(Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"), "[EMAIL]")
        .replace(Regex("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"), "[CARD]")
        .replace(Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"), "[SSN]")
        // Matches E.164 (+1xxxxxxxxxx) and NANP (xxx-xxx-xxxx / xxxxxxxxxx) only
        .replace(Regex("\\b(\\+1)?[2-9]\\d{2}[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b"), "[PHONE]")
}

/**
 * Truncate long strings, appending "…" when the limit is exceeded.
 */
fun String.truncate(maxLength: Int = 100): String {
    return if (this.length > maxLength) "${substring(0, maxLength)}…" else this
}