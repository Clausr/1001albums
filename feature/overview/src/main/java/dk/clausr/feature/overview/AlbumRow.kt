package dk.clausr.feature.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.ui.components.AlbumThumb
import dk.clausr.a1001albumsgenerator.ui.preview.PreviewSharedTransitionLayout
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.feature.overview.preview.historicAlbumPreviewData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AlbumRow(
    title: String,
    albums: ImmutableList<HistoricAlbum>,
    onClickAlbum: (slug: String, listName: String) -> Unit,
    onClickPlay: (link: String) -> Unit,
    streamingPlatform: StreamingPlatform,
    tertiaryTextTransform: (HistoricAlbum) -> String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(
                items = albums,
                key = { i, album -> "$title-${album.album.slug}-$i" },
            ) { _, album ->
                val streamingLink = StreamingServices
                    .from(album.album)
                    .getStreamingLinkFor(streamingPlatform)

                val playClick = streamingLink?.let {
                    {
                        onClickPlay(it)
                    }
                }

                AlbumThumb(
                    modifier = Modifier.width(120.dp),
                    album = album,
                    onClick = { onClickAlbum(album.album.slug, title) },
                    onClickPlay = playClick,
                    tertiaryText = tertiaryTextTransform(album),
                    listName = title,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    OagTheme {
        PreviewSharedTransitionLayout {
            AlbumRow(
                title = "Title goes here",
                albums = persistentListOf(
                    historicAlbumPreviewData(slug = "0"),
                    historicAlbumPreviewData(slug = "1"),
                    historicAlbumPreviewData(slug = "2"),
                    historicAlbumPreviewData(slug = "3"),
                ),
                onClickAlbum = { _, _ -> },
                streamingPlatform = StreamingPlatform.Spotify,
                tertiaryTextTransform = { null },
                onClickPlay = {},
            )
        }
    }
}
