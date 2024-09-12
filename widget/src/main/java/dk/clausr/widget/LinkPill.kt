package dk.clausr.widget

import androidx.compose.runtime.Composable
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.extensions.openUrlAction
import dk.clausr.a1001albumsgenerator.ui.R as uiR

@Composable
fun LinkPill(
    preferredStreamingPlatform: StreamingPlatform?,
    wikipediaLink: String,
    streamingServices: StreamingServices,
    projectUrl: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            GlanceModifier
                .background(
                    imageProvider = ImageProvider(R.drawable.pill_background),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.background),
                ),
        ) {
            CircleIconButton(
                imageProvider = ImageProvider(uiR.drawable.ic_wiki),
                contentDescription = context.getString(R.string.a11y_content_description_wikipedia_link),
                onClick = openUrlAction(wikipediaLink),
            )

            streamingServices.services.firstOrNull { it.platform == preferredStreamingPlatform }
                ?.let { link ->
                    CircleIconButton(
                        imageProvider = ImageProvider(uiR.drawable.ic_play_arrow),
                        contentDescription = link.platform.name,
                        onClick = openUrlAction(link.streamingLink),
                    )
                }

            CircleIconButton(
                imageProvider = ImageProvider(uiR.drawable.ic_open_external),
                contentDescription = context.getString(R.string.a11y_content_description_external_link),
                onClick = openUrlAction(projectUrl),
            )
        }
    }
}
