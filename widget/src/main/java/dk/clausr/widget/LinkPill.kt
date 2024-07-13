package dk.clausr.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import dk.clausr.core.common.BuildConfig
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.extensions.icon
import dk.clausr.extensions.openUrlAction
import dk.clausr.a1001albumsgenerator.ui.R as uiR

@Composable
fun LinkPill(
    preferredStreamingPlatform: StreamingPlatform,
    wikipediaLink: String,
    streamingServices: StreamingServices,
    projectUrl: String,
    onForceUpdateWidget: () -> Unit = {},
) {
    Row(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            GlanceModifier
                .background(GlanceTheme.colors.background)
                .cornerRadius(100.dp)
        ) {
            CircleIconButton(
                imageProvider = ImageProvider(uiR.drawable.ic_wiki),
                contentDescription = "Wikipedia",
                onClick = openUrlAction(wikipediaLink),
            )

            streamingServices.services.firstOrNull { it.platform == preferredStreamingPlatform }
                ?.let { link ->
                    CircleIconButton(
                        imageProvider = ImageProvider(link.platform.icon()),
                        contentDescription = link.platform.name,
                        onClick = openUrlAction(link.streamingLink),
                    )
                }

            CircleIconButton(
                imageProvider = ImageProvider(uiR.drawable.ic_open_external),
                contentDescription = "Open website",
                onClick = openUrlAction(projectUrl),
            )

            if (BuildConfig.DEBUG) {
                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.baseline_refresh_24),
                    contentDescription = "Update widget",
                    onClick = { onForceUpdateWidget() })
            }
        }
    }
}
