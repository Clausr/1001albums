package dk.clausr.a1001albumsgenerator.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import dk.clausr.a1001albumsgenerator.ui.R
import dk.clausr.a1001albumsgenerator.ui.preview.consistentRandomColor

@Composable
fun AlbumCover(
    coverUrl: String?,
    albumSlug: String?,
    modifier: Modifier = Modifier,
    placeholderResId: Int = R.drawable.album_cover_placeholder,
    shape: Shape = RoundedCornerShape(4.dp),
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    val sharedModifier = modifier
        .fillMaxWidth()
        .clip(shape = shape)

    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(placeholderResId),
            contentDescription = null,
            contentScale = contentScale,
            modifier = sharedModifier
                .background(consistentRandomColor(albumSlug.orEmpty())),
        )
    } else {
        val placeholder = rememberAlbumCoverPlaceholder(placeholderResId)
        AsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .data(coverUrl)
                .crossfade(true)
                .placeholderMemoryCacheKey(albumSlug)
                .memoryCacheKey(albumSlug)
                .build(),
            contentDescription = null,
            // Not `placeholder =`, as that would replace the cover cached under placeholderMemoryCacheKey
            transform = { state ->
                when (state) {
                    is AsyncImagePainter.State.Loading -> if (state.painter == null) state.copy(painter = placeholder) else state
                    is AsyncImagePainter.State.Error -> state.copy(painter = placeholder)
                    else -> state
                }
            },
            modifier = sharedModifier,
            contentScale = contentScale,
        )
    }
}

/**
 * The cover shown while loading, or when there is none: the placeholder icon in the text color on a surface colored
 * background, so it's visible in dark mode and doesn't leave a see-through square behind shadows.
 */
@Composable
fun rememberAlbumCoverPlaceholder(placeholderResId: Int = R.drawable.album_cover_placeholder): Painter {
    val icon = painterResource(placeholderResId)
    val background = MaterialTheme.colorScheme.surface
    val tint = MaterialTheme.colorScheme.onSurface
    return remember(icon, background, tint) {
        AlbumCoverPlaceholderPainter(icon = icon, background = background, tint = tint)
    }
}

private class AlbumCoverPlaceholderPainter(
    private val icon: Painter,
    private val background: Color,
    tint: Color,
) : Painter() {
    private val colorFilter = ColorFilter.tint(tint)

    override val intrinsicSize: Size
        get() = icon.intrinsicSize

    override fun DrawScope.onDraw() {
        drawRect(background)
        with(icon) { draw(size, colorFilter = colorFilter) }
    }
}

@Preview
@Composable
private fun AlbumCoverPreview() {
    AlbumCover(
        coverUrl = null,
        albumSlug = "null",
    )
}
