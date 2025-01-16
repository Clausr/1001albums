package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.onboarding.components.OnboardingTitle
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
import dk.clausr.a1001albumsgenerator.ui.extensions.conditional
import dk.clausr.a1001albumsgenerator.ui.extensions.logClickEvent
import dk.clausr.a1001albumsgenerator.ui.extensions.logListItemSelected
import dk.clausr.a1001albumsgenerator.ui.helper.displayName
import dk.clausr.a1001albumsgenerator.ui.helper.icon
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.StreamingPlatform

@Composable
internal fun StreamingServiceScreen(
    onSetStreamingPlatform: (StreamingPlatform) -> Unit,
    modifier: Modifier = Modifier,
    preselectedPlatform: StreamingPlatform? = null,
    showSelectButton: Boolean = true,
) {
    val analyticsHelper = LocalAnalyticsHelper.current

    if (showSelectButton) {
        TrackScreenViewEvent(screenName = "StreamingServiceScreen")
    }
    var selectedPlatform: StreamingPlatform? by remember(preselectedPlatform) {
        mutableStateOf(preselectedPlatform)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OnboardingTitle(text = stringResource(R.string.streaming_service_title))

        Text(
            text = stringResource(R.string.streaming_service_subtitle),
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
        ) {
            StreamingPlatform.entries.filterNot { it == StreamingPlatform.Undefined }.forEach { platform ->
                val isSelected = platform == selectedPlatform
                val itemColor = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current
                val displayName = platform.displayName()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .conditional(isSelected) {
                            background(itemColor.copy(alpha = 0.1f))
                        }
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                analyticsHelper.logListItemSelected(
                                    listName = "Streaming service",
                                    itemName = displayName
                                )
                                selectedPlatform = platform
                                if (!showSelectButton) {
                                    onSetStreamingPlatform(platform)
                                }
                            },
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        tint = itemColor,
                        painter = painterResource(id = platform.icon()),
                        contentDescription = null,
                    )
                    Text(
                        text = displayName,
                        color = itemColor,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (showSelectButton) {
            Button(
                enabled = selectedPlatform != null,
                onClick = {
                    analyticsHelper.logClickEvent("Select streaming service")
                    onSetStreamingPlatform(selectedPlatform!!)
                },
            ) {
                Text(stringResource(R.string.select_streaming_service_button_title))
            }
        }
    }
}

@Preview
@Composable
private fun StreamingServiceScreenPreview() {
    OagTheme {
        StreamingServiceScreen(
            onSetStreamingPlatform = {},
            preselectedPlatform = StreamingPlatform.AppleMusic,
        )
    }
}
