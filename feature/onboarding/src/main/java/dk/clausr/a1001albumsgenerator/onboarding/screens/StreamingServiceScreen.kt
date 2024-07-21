package dk.clausr.a1001albumsgenerator.onboarding.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.onboarding.components.OnboardingTitle
import dk.clausr.a1001albumsgenerator.ui.helper.icon
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.StreamingPlatform

@Composable
internal fun StreamingServiceScreen(
    onSetStreamingPlatform: (StreamingPlatform) -> Unit,
    modifier: Modifier = Modifier,
    preselectedPlatform: StreamingPlatform? = null,
) {
    var selectedPlatform: StreamingPlatform? by remember {
        mutableStateOf(preselectedPlatform)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OnboardingTitle(text = "Streaming service")

        Text(
            text = "Select your preferred streaming service",
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
        ) {
            StreamingPlatform.entries.forEach { platform ->
                val isSelected = platform == selectedPlatform
                val itemColor = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .selectable(
                            selected = isSelected,
                            onClick = { selectedPlatform = platform },
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
                    Text(text = platform.name, color = itemColor)
                }
            }
        }

        Button(
            enabled = selectedPlatform != null,
            onClick = {
                onSetStreamingPlatform(selectedPlatform!!)
            },
        ) {
            Text("Select")
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
