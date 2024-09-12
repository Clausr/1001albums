package dk.clausr.feature.overview

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.clausr.core.common.extensions.toRelativeTimeString
import dk.clausr.core.model.Notification
import dk.clausr.feature.overview.notifications.getBody
import dk.clausr.feature.overview.notifications.getTitle
import kotlinx.collections.immutable.ImmutableList

@Composable
fun NotificationUpperSheet(
    showNotifications: Boolean,
    onDismiss: () -> Unit,
    clearNotifications: () -> Unit,
    notifications: ImmutableList<Notification>,
    modifier: Modifier = Modifier,
) {
    // Scrim
    AnimatedVisibility(
        visible = showNotifications,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        BackHandler(onBack = onDismiss)

        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onDismiss,
                ),
        )
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = showNotifications,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
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
                    AnimatedVisibility(
                        notifications.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        TextButton(onClick = clearNotifications) { Text(stringResource(R.string.notifications_clear_all_button_title)) }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                NotificationSheetContent(notifications = notifications)
            }
        }
    }
}

@Composable
private fun NotificationSheetContent(
    notifications: ImmutableList<Notification>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (notifications.isEmpty()) {
            // Empty state
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
        } else {
            items(items = notifications) { notification ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .animateItem(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        Text(
                            text = notification.getTitle(context) ?: "",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(text = notification.getBody(context) ?: "")
                        Text(
                            text = notification.createdAt.toRelativeTimeString(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}
