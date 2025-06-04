package dk.clausr.feature.overview.notifications

import kotlinx.collections.immutable.PersistentList

sealed interface NotificationViewState {
    data object EmptyState : NotificationViewState
    data class ShowNotifications(val notifications: PersistentList<NotificationRowData>) : NotificationViewState
}
