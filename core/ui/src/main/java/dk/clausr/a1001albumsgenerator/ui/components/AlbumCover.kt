package dk.clausr.a1001albumsgenerator.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
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
        .aspectRatio(1f)
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
        AsyncImage(
            model = ImageRequest
                .Builder(LocalContext.current)
                .data(coverUrl)
                .crossfade(true)
                .placeholderMemoryCacheKey(albumSlug)
                .memoryCacheKey(albumSlug)
                .placeholder(placeholderResId)
                .build(),
            contentDescription = null,
            modifier = sharedModifier,
            contentScale = contentScale,
        )
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
