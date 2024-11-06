package dk.clausr.extensions

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import dk.clausr.widget.AlbumCoverWidget
import dk.clausr.widget.AlbumCoverWidgetReceiver

suspend fun Context.askToAddToHomeScreen() {
    GlanceAppWidgetManager(this)
        .requestPinGlanceAppWidget(
            receiver = AlbumCoverWidgetReceiver::class.java,
            preview = AlbumCoverWidget(),
            previewState = null,
            successCallback = null,
        )
}
