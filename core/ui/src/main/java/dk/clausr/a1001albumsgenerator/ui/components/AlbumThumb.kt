package dk.clausr.a1001albumsgenerator.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dk.clausr.a1001albumsgenerator.ui.R
import dk.clausr.a1001albumsgenerator.ui.extensions.forwardingPainter
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.HistoricAlbum

@Composable
fun AlbumThumb(
    artist: String,
    name: String,
    coverUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    Surface(
        modifier = modifier
            .width(size),
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                placeholder = forwardingPainter(
                    painterResource(id = R.drawable.album_cover_placeholder),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .size(size)
                    .clip(shape = RoundedCornerShape(4.dp)),
                contentScale = ContentScale.FillWidth,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun AlbumThumb(
    album: HistoricAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    AlbumThumb(
        artist = album.album.artist,
        name = album.album.name,
        coverUrl = album.album.imageUrl,
        onClick = onClick,
        size = size,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun AlbumThumbPreview() {
    OagTheme {
        AlbumThumb(
            artist = "Black Sabbath",
            name = "Paranoid",
            coverUrl = "https://i.scdn.co/image/ab2eae28bb2a55667ee727711aeccc7f37498414",
            onClick = {},
        )
    }
}