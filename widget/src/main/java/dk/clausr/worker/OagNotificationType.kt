package dk.clausr.worker

sealed class OagNotificationType(val id: Int, val channelId: String) {
    data object PeriodicSync : OagNotificationType(
        id = PERIODIC_SYNC_NOTIFICATION_ID,
        channelId = PERIODIC_SYNC_NOTIFICATION_CHANNEL_ID
    )

    data object BurstSync : OagNotificationType(
        id = BURST_SYNC_NOTIFICATION_ID,
        channelId = BURST_SYNC_NOTIFICATION_CHANNEL_ID
    )

    data object UpdateWidgetState : OagNotificationType(
        id = UPDATE_WIDGET_WORKER_NOTIFICATION_ID,
        channelId = UPDATE_WIDGET_WORKER_NOTIFICATION_CHANNEL_ID
    )
}

const val PERIODIC_SYNC_NOTIFICATION_ID = 0
const val BURST_SYNC_NOTIFICATION_ID = 1
const val UPDATE_WIDGET_WORKER_NOTIFICATION_ID = 2
private const val PERIODIC_SYNC_NOTIFICATION_CHANNEL_ID = "PeriodicSyncNotificationChannel"
private const val BURST_SYNC_NOTIFICATION_CHANNEL_ID = "BurstSyncNotificationChannel"
private const val UPDATE_WIDGET_WORKER_NOTIFICATION_CHANNEL_ID = "UpdateWidgetWorkerNotificationChannel"
