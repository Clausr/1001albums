package dk.clausr.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import dk.clausr.extensions.openWebsite

@Composable
internal fun CurrentAlbum(coverUrl: String, projectId: String) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier.fillMaxSize().clickable {
            context.openWebsite(projectId)
        }, contentAlignment = Alignment.BottomCenter
    ) {
        CoverImage(
            modifier = GlanceModifier.fillMaxSize(), coverUrl = coverUrl
        )
    }
}
