package dk.clausr.feature.overview.notifications

sealed interface NotificationViewEffect {
    data object HideNotifications : NotificationViewEffect
}