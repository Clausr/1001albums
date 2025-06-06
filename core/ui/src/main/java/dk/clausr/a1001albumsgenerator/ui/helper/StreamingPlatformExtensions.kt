package dk.clausr.a1001albumsgenerator.ui.helper

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
    StreamingPlatform.Undefined -> -1
}

@StringRes
fun StreamingPlatform.displayNameRes(): Int? = when (this) {
    StreamingPlatform.AmazonMusic -> R.string.platform_amazon_music
    StreamingPlatform.AppleMusic -> R.string.platform_apple_music
    StreamingPlatform.Deezer -> R.string.platform_deezer
    StreamingPlatform.Spotify -> R.string.platform_spotify
    StreamingPlatform.Tidal -> R.string.platform_tidal
    StreamingPlatform.YouTubeMusic -> R.string.platform_youtube_music
    StreamingPlatform.Qobuz -> R.string.platform_qobuz
    StreamingPlatform.Undefined -> null
}

@Composable
fun StreamingPlatform.displayName(): String = displayNameRes()?.let { displayNameResId ->
    stringResource(displayNameResId)
}.orEmpty()
