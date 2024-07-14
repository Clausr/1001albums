package dk.clausr.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding

@Composable
internal fun ErrorState(
    modifier: GlanceModifier = GlanceModifier,
    retry: () -> Unit = {},
) {
    Box(
        modifier
            .fillMaxSize()
            .background(GlanceTheme.colors.errorContainer)
            .clickable(retry),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = GlanceModifier.fillMaxSize().padding(20.dp),
            provider = ImageProvider(R.drawable.heart_broken),
            contentDescription = "Error",
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onErrorContainer),
        )
    }
}
