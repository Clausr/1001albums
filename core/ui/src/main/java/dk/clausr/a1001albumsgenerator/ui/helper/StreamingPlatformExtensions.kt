package dk.clausr.a1001albumsgenerator.ui.helper

import androidx.annotation.DrawableRes
import dk.clausr.a1001albumsgenerator.ui.R
import dk.clausr.core.model.StreamingPlatform


@DrawableRes
fun StreamingPlatform.icon(): Int = when (this) {
    StreamingPlatform.AmazonMusic -> R.drawable.ic_amazon_music
    StreamingPlatform.AppleMusic -> R.drawable.ic_apple_music
    StreamingPlatform.Deezer -> R.drawable.ic_deezer_circle
    StreamingPlatform.Spotify -> R.drawable.ic_spotify
    StreamingPlatform.Tidal -> R.drawable.ic_tidal
    StreamingPlatform.YouTubeMusic -> R.drawable.ic_youtube_music
    StreamingPlatform.Qobuz -> R.drawable.ic_qobuz
}
