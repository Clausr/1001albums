@file:OptIn(ExperimentalSharedTransitionApi::class)

package dk.clausr.a1001albumsgenerator.ui.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dk.clausr.a1001albumsgenerator.ui.R
import dk.clausr.a1001albumsgenerator.ui.extensions.forwardingPainter
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.HistoricAlbum

@Composable
fun AlbumThumb(
    albumSlug: String,
    artist: String,
    name: String,
    coverUrl: String,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    with(sharedTransitionScope) {
        Surface(
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "bounds-$albumSlug"),
                    animatedVisibilityScope = animatedContentScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(4.dp))
                )
                .width(size),
            onClick = onClick,
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(
                Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverUrl)
                        .crossfade(true)
                        .placeholderMemoryCacheKey(albumSlug)
                        .memoryCacheKey(albumSlug)
                        .build(),
                    contentDescription = null,
                    placeholder = forwardingPainter(
                        painterResource(id = R.drawable.album_cover_placeholder),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                    ),
                    modifier = Modifier
                        .size(size)
                        .clip(shape = RoundedCornerShape(4.dp))
                        .sharedElement(
                            state = rememberSharedContentState(key = "cover-$albumSlug"),
                            animatedVisibilityScope = animatedContentScope,
//                            boundsTransform = { _, _ ->
//                                spring(dampingRatio = 0.8f, stiffness = 380f)
//                            },
                        ),
                    contentScale = ContentScale.Crop,
                )

                Text(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "title-$albumSlug"),
                            animatedVisibilityScope = animatedContentScope,
                            renderInOverlayDuringTransition = false,
                        )
                        .fillMaxWidth(),
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "artist-$albumSlug"),
                            animatedVisibilityScope = animatedContentScope,
                            renderInOverlayDuringTransition = false,
                        )
                        .fillMaxWidth(),
                    text = artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun AlbumThumb(
    album: HistoricAlbum,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    AlbumThumb(
        albumSlug = album.album.slug,
        artist = album.album.artist,
        name = album.album.name,
        coverUrl = album.album.imageUrl,
        onClick = onClick,
        size = size,
        modifier = modifier,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
    )
}

@Preview
@Composable
private fun AlbumThumbPreview() {
    OagTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                AlbumThumb(
                    albumSlug = "slug",
                    artist = "Black Sabbath",
                    name = "Paranoid",
                    coverUrl = "https://i.scdn.co/image/ab2eae28bb2a55667ee727711aeccc7f37498414",
                    onClick = {},
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedVisibility,
                )
            }
        }
    }
}