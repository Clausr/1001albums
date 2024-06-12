package dk.clausr.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@Composable
fun Widget2(
    artist: String,

    ) {
    Scaffold(
        backgroundColor = GlanceTheme.colors.widgetBackground,
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(R.drawable.heart_broken),
                title = artist
            )
        }
    ) {
        Text(text = "Hello world", style = TextStyle(color = GlanceTheme.colors.onSurface))
    }
}

