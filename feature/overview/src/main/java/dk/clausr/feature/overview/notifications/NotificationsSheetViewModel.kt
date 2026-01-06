package dk.clausr.feature.overview.notifications

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.clausr.core.common.extensions.toLocalizedDateTime
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Notification
import dk.clausr.widget.AlbumCoverWidget
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationsSheetViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    // Temporary counter to hold on to read notifications
    private val unreadNotificationCount = MutableStateFlow(0)
    private val showAllNotifications = MutableStateFlow(false)

    val viewState = combine(
        notificationRepository.notifications,
        notificationRepository.unreadNotifications,
        unreadNotificationCount,
        showAllNotifications,
    ) {
            readNotifications,
            unreadNotifications,
            internalUnreadNotificationCount,
            showAllNotifications,
        ->

        if (showAllNotifications) {
            NotificationViewState.ShowNotifications(
                notifications = readNotifications.map { it.mapToRowData() }.toPersistentList(),
            )
        } else if (unreadNotifications.isEmpty() && internalUnreadNotificationCount == 0) {
            NotificationViewState.EmptyState
        } else {
            // Show previously read notifications
            if (unreadNotifications.isEmpty() && internalUnreadNotificationCount > 0) {
                NotificationViewState.ShowNotifications(
                    notifications = readNotifications.take(internalUnreadNotificationCount).map { it.mapToRowData() }.toPersistentList(),
                )
            } else {
                // Set amount of unread notifications to be able to show them in the read state
                // unreadNotificationCount.value = unreadNotifications.size

                NotificationViewState.ShowNotifications(
                    notifications = unreadNotifications.map { it.mapToRowData() }.toPersistentList(),
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationViewState.EmptyState,
    )

    init {
        // Clear notifications when opening the sheet
        viewModelScope.launch {
            notificationRepository.unreadNotifications.collect { unreadNotifications ->
                if (unreadNotifications.isNotEmpty()) {
                    unreadNotificationCount.value = unreadNotifications.size

                    clearUnreadNotifications()
                }
            }
        }
    }

    private suspend fun clearUnreadNotifications() {
        oagRepository.projectId.collect { projectId ->
            val id = projectId ?: return@collect
            notificationRepository.readAll(id)
                .doOnSuccess {
                    Timber.d("Notifications marked as read, update widget.")
                    AlbumCoverWidget().updateAll(context = context)
                }
                .doOnFailure {
                    Timber.e(it.cause, "Could not read all notifications")
                }
        }
    }

    internal fun toggleAllNotifications() {
        showAllNotifications.value = !showAllNotifications.value
    }

    private fun Notification.mapToRowData(): NotificationRowData {
        return NotificationRowData(
            title = getTitle(context).orEmpty(),
            createdAt = createdAt.toLocalizedDateTime(),
            body = getBody(context).orEmpty(),
            onClickEnabled = isClickable,
            notificationData = data,
            read = read,
        )
    }
}
