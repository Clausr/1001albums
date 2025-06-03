package dk.clausr.feature.overview.notifications

import dk.clausr.core.model.NotificationData

data class NotificationRowData(
    val title: String,
    val createdAt: String,
    val body: String,
    val onClickEnabled: Boolean,
    val notificationData: NotificationData?,
    val read: Boolean,
)
