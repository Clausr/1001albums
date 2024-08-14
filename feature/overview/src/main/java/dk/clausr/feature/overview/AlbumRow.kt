package dk.clausr.feature.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.ui.components.AlbumThumb
import dk.clausr.core.common.android.openLink
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AlbumRow(
    title: String,
    albums: ImmutableList<HistoricAlbum>,
    onClickAlbum: (slug: String) -> Unit,
    streamingPlatform: StreamingPlatform,
    tertiaryTextTransform: (HistoricAlbum) -> String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
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
            items(items = albums) { album ->
                val streamingLink = StreamingServices
                    .from(album.album)
                    .getStreamingLinkFor(streamingPlatform)

                val onClickPlay = streamingLink?.let {
                    {
                        context.openLink(streamingLink)
                    }
                }

                AlbumThumb(
                    album = album,
                    onClick = { onClickAlbum(album.album.slug) },
                    onClickPlay = onClickPlay,
                    tertiaryText = tertiaryTextTransform(album),
                )
            }
        }
    }
}
