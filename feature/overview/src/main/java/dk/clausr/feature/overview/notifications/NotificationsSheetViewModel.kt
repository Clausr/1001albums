package dk.clausr.feature.overview.notifications

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsEvent
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsHelper
import dk.clausr.core.common.extensions.toLocalizedDateTime
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Notification
import dk.clausr.widget.AlbumCoverWidget
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationsSheetViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {
    private val _viewEffect = Channel<NotificationViewEffect>(Channel.BUFFERED)
    val viewEffect = _viewEffect.receiveAsFlow()

    val viewState = combine(
        notificationRepository.notifications,
        notificationRepository.unreadNotifications
    ) { readNotifications, unreadNotifications ->
        if (unreadNotifications.isNotEmpty()) {
            NotificationViewState.ShowNotification(
                unreadNotifications = unreadNotifications.map { it.mapToRowData() }.toPersistentList(),
                readNotifications = readNotifications.map { it.mapToRowData() }.toPersistentList(),
                showClearButton = unreadNotifications.isNotEmpty(),
            )
        } else {
            NotificationViewState.EmptyState
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotificationViewState.EmptyState,
    )

    fun clearUnreadNotifications() {
        analyticsHelper.logEvent(AnalyticsEvent("Clear notifications"))
        viewModelScope.launch {
            _viewEffect.send(NotificationViewEffect.HideNotifications)

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