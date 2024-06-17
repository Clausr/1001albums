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
import androidx.glance.layout.width
import dk.clausr.core.model.StreamingLink
import dk.clausr.core.model.StreamingLinks
import dk.clausr.extensions.clickableOpenUrl
import dk.clausr.extensions.openSomeActivity

@Composable
fun LinkPill(
    wikipediaLink: String,
    streamingLinks: StreamingLinks,
    projectUrl: String,
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

        streamingLinks.links.forEach { link ->
            StreamingService(streamingLink = link)
        }

        Spacer(GlanceModifier.width(16.dp))
        Image(
            provider = ImageProvider(R.drawable.ic_open_external),
            contentDescription = "Open website",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
            modifier = GlanceModifier.clickableOpenUrl(projectUrl)
        )
    }
}

@Composable
fun StreamingService(
    streamingLink: StreamingLink,
) {
    Spacer(GlanceModifier.width(16.dp))
    Image(
        provider = ImageProvider(R.drawable.ic_tidal),
        contentDescription = streamingLink.name,
        colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
        modifier = GlanceModifier.clickableOpenUrl(streamingLink.link),
    )
}