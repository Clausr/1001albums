package dk.clausr.feature.overview.notifications

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

sealed class NotificationViewState(open val showClearButton: Boolean = false) {
    data object EmptyState : NotificationViewState()
    data class ShowNotification(
        val unreadNotifications: PersistentList<NotificationRowData> = persistentListOf(),
        val readNotifications: PersistentList<NotificationRowData> = persistentListOf(),
        override val showClearButton: Boolean = false,
    ) : NotificationViewState(showClearButton = showClearButton)
}
