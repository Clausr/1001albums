package dk.clausr.extensions

import androidx.annotation.DrawableRes
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.widget.R
import dk.clausr.a1001albumsgenerator.ui.R as uiR

@DrawableRes
fun StreamingPlatform.icon(): Int = when (this) {
    StreamingPlatform.None -> R.drawable.heart_broken
    StreamingPlatform.AmazonMusic -> uiR.drawable.ic_amazon_music
    StreamingPlatform.AppleMusic -> uiR.drawable.ic_apple_music
    StreamingPlatform.Deezer -> uiR.drawable.ic_deezer_circle
    StreamingPlatform.Spotify -> uiR.drawable.ic_spotify
    StreamingPlatform.Tidal -> uiR.drawable.ic_tidal
    StreamingPlatform.YouTubeMusic -> uiR.drawable.ic_youtube_music
    StreamingPlatform.Qobuz -> uiR.drawable.ic_qobuz
}
