package io.github.alimsrepo.crashguard.domain.config

/**
 * Notification configuration
 */
data class NotificationConfig(
    val channelId: String = "crash_guard_channel",
    val channelName: String = "Crash Notifications",
    val notificationTitle: String = "App Crashed",
    val notificationMessage: String = "The app encountered an error",
    val smallIcon: Int,
    val autoCancel: Boolean = true
)