package dk.clausr.a1001albumsgenerator.ui.components

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dk.clausr.a1001albumsgenerator.ui.preview.PreviewSharedTransitionLayout
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.HistoricAlbum
import kotlin.random.Random
import dk.clausr.a1001albumsgenerator.ui.R as uiR

@Composable
fun AlbumThumb(
    albumSlug: String,
    artist: String,
    name: String,
    coverUrl: String,
    tertiaryText: String?,
    onClick: () -> Unit,
    onClickPlay: (() -> Unit)?,
    modifier: Modifier = Modifier,
    listName: String = "List",
) {
    val animatedContentScope = LocalNavAnimatedVisibilityScope.current
    with(LocalSharedTransitionScope.current) {
        Surface(
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "$listName-bounds-$albumSlug"),
                    animatedVisibilityScope = animatedContentScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(4.dp)),
                ),
            onClick = onClick,
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(4.dp),
        ) {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    if (LocalInspectionMode.current) {
                        fun randomColor() = Color(Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt()))
                        Image(
                            painter = painterResource(uiR.drawable.album_cover_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(randomColor())
                                .aspectRatio(1f)
                                .clip(shape = RoundedCornerShape(4.dp)),
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest
                                .Builder(LocalContext.current)
                                .data(coverUrl)
                                .crossfade(true)
                                .placeholderMemoryCacheKey(albumSlug)
                                .memoryCacheKey(albumSlug)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .sharedElement(
                                    state = rememberSharedContentState(key = "$listName-cover-$albumSlug"),
                                    animatedVisibilityScope = animatedContentScope,
                                )
                                .clip(shape = RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.FillWidth,
                        )
                    }

                    onClickPlay?.let { onClickPlay ->
                        FilledTonalIconButton(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(40.dp)
                                .sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "$listName-play-$albumSlug"),
                                    animatedVisibilityScope = animatedContentScope,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                )
                                .clip(CircleShape),
                            onClick = onClickPlay,
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    }
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "$listName-title-$albumSlug"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                contentScale = ContentScale.FillHeight,
                                alignment = Alignment.Center,
                            ),
                        ),
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "$listName-artist-$albumSlug"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                contentScale = ContentScale.FillHeight,
                                alignment = Alignment.Center,
                            ),
                        ),
                    text = artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                tertiaryText?.let {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "$listName-date-$albumSlug"),
                                animatedVisibilityScope = animatedContentScope,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                    contentScale = ContentScale.FillHeight,
                                    alignment = Alignment.Center,
                                ),
                            ),
                        text = it,
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumThumb(
    album: HistoricAlbum,
    onClick: () -> Unit,
    onClickPlay: (() -> Unit)?,
    modifier: Modifier = Modifier,
    tertiaryText: String? = null,
    listName: String = "List",
) {
    AlbumThumb(
        albumSlug = album.album.slug,
        artist = album.album.artist,
        name = album.album.name,
        coverUrl = album.album.imageUrl,
        tertiaryText = tertiaryText,
        onClick = onClick,
        onClickPlay = onClickPlay,
        modifier = modifier,
        listName = listName,
    )
}

@Preview(widthDp = 200)
@Composable
private fun AlbumThumbPreview() {
    OagTheme {
        PreviewSharedTransitionLayout {
            AlbumThumb(
                albumSlug = "slug",
                artist = "Black Sabbath",
                name = "Paranoid",
                coverUrl = "https://i.scdn.co/image/ab2eae28bb2a55667ee727711aeccc7f37498414",
                tertiaryText = "Some tertiary text",
                onClick = {},
                onClickPlay = {},
            )
        }
    }
}
