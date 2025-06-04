package dk.clausr.feature.overview.notifications

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.NotificationData
import dk.clausr.feature.overview.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DISMISS_DELAY = 300L

@Composable
fun NotificationUpperSheet(
    onDismiss: () -> Unit,
    onNotificationClick: (NotificationData) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsSheetViewModel = hiltViewModel(),
) {
    TrackScreenViewEvent(screenName = "Notifications")
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    var showNotifications by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    fun onClose() = coroutineScope.launch {
        showNotifications = false
        delay(DISMISS_DELAY)
        onDismiss()
    }

    LaunchedEffect(Unit) { showNotifications = true }

    // Scrim
    AnimatedVisibility(
        visible = showNotifications,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        BackHandler(onBack = ::onClose)

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = ::onClose,
                ),
        )
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = showNotifications,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
    ) {
        NotificationSheetContent(
            viewState = viewState,
            onNotificationClick = onNotificationClick,
            onClose = { onClose() },
        )
    }
}

@Composable
private fun NotificationSheetContent(
    viewState: NotificationViewState,
    onNotificationClick: (NotificationData) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = Modifier.animateContentSize(),
        shape = MaterialTheme.shapes.medium.copy(
            topStart = CornerSize(0.dp),
            topEnd = CornerSize(0.dp),
        ),
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.notifications_title),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            LazyColumn(
                modifier = modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (viewState) {
                    NotificationViewState.EmptyState -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                            ) {
                                Text(
                                    text = stringResource(R.string.notifications_empty_state_title),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    is NotificationViewState.ShowNotifications -> {
                        items(items = viewState.notifications) { notification ->
                            NotificationRow(
                                notification = notification,
                                onNotificationClick = onNotificationClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: NotificationRowData,
    onNotificationClick: (NotificationData) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = notification.onClickEnabled) {
                notification.notificationData?.let(onNotificationClick)
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.labelLarge,
            )

            Text(text = notification.body)
            Text(
                text = notification.createdAt,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        if (notification.onClickEnabled) {
            Image(
                imageVector = Icons.AutoMirrored.Default.OpenInNew,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}

@Composable
@Preview
private fun EmptyStatePreview() {
    OagTheme {
        NotificationSheetContent(
            viewState = NotificationViewState.EmptyState,
            onNotificationClick = {},
            onClose = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun NotificationsStatePreview() {
    OagTheme {
        NotificationSheetContent(
            viewState = NotificationViewState.ShowNotifications(
                notifications = persistentListOf(
                    NotificationRowData(
                        createdAt = "January 1, 1970 at 00:02",
                        read = false,
                        title = "Title",
                        body = "Body",
                        notificationData = null,
                        onClickEnabled = false,
                    ),
                    NotificationRowData(
                        createdAt = "January 1, 1970 at 00:01",
                        read = false,
                        title = "Title",
                        body = "Body with action",
                        notificationData = null,
                        onClickEnabled = true,
                    ),
                ),
            ),
            onNotificationClick = {},
            onClose = {},
        )
    }
}
