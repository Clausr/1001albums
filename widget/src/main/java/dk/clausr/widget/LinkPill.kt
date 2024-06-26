package dk.clausr.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingService
import dk.clausr.core.model.StreamingServices
import dk.clausr.extensions.clickableOpenUrl
import dk.clausr.extensions.icon
import dk.clausr.extensions.openSomeActivity

@Composable
fun LinkPill(
    wikipediaLink: String,
    streamingServices: StreamingServices,
    projectUrl: String,
    onForceUpdateWidget: () -> Unit = {},
) {
    val context = LocalContext.current

    Row(
        GlanceModifier
            .background(GlanceTheme.colors.background)
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .cornerRadius(100.dp)
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_wiki),
            contentDescription = "Wikipedia",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
            modifier = GlanceModifier
                .clickable {
                    context.openSomeActivity(wikipediaLink)
                })

        streamingServices.services.filter { it.platform == StreamingPlatform.Tidal }
            .forEach { link ->
                StreamingService(streamingService = link)
            }

        Spacer(GlanceModifier.width(16.dp))
        Image(
            provider = ImageProvider(R.drawable.ic_open_external),
            contentDescription = "Open website",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
            modifier = GlanceModifier.clickableOpenUrl(projectUrl)
        )

        Spacer(GlanceModifier.width(24.dp))
        Image(
            provider = ImageProvider(R.drawable.baseline_refresh_24),
            contentDescription = "Update widget",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
            modifier = GlanceModifier
                .clickable { onForceUpdateWidget() }
        )
    }
}

@Composable
fun StreamingService(
    streamingService: StreamingService,
) {
    Spacer(GlanceModifier.width(16.dp))
    Image(
        provider = ImageProvider(streamingService.platform.icon()),
        contentDescription = streamingService.platform.name,
        colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
        modifier = GlanceModifier
            .size(20.dp)
            .clickableOpenUrl(streamingService.streamingLink),
    )
}