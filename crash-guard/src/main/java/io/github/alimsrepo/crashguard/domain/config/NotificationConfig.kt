package io.github.alimsrepo.crashguard.domain.config

import io.github.alimsrepo.crashguard.utils.CrashGuardConstants

/**
 * Notification configuration
 */
data class NotificationConfig(
    val channelId: String = CrashGuardConstants.NOTIFICATION_CHANNEL_ID,
    val channelName: String = "Crash Notifications",
    val notificationTitle: String = "App Crashed",
    val notificationMessage: String = "The app encountered an error",
    val smallIcon: Int,
    val autoCancel: Boolean = true
)