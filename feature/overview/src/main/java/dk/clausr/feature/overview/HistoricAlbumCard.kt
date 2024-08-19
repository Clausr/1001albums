package dk.clausr.feature.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.feature.overview.preview.historicAlbumPreviewData
import dk.clausr.a1001albumsgenerator.ui.R as uiR

@Composable
fun HistoricAlbumCard(
    historicAlbum: HistoricAlbum,
    preferredStreamingPlatform: StreamingPlatform,
    onClick: () -> Unit,
    openLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    val album = historicAlbum.album
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 8.dp,
            topEnd = 8.dp,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                modifier = Modifier.size(100.dp),
                model = ImageRequest.Builder(
                    LocalContext.current,
                ).data(album.imageUrl).crossfade(true).build(),
                contentDescription = "Album cover image",
                contentScale = ContentScale.FillWidth,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
            ) {
                Text(
                    text = album.artist,
                )
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = historicAlbum.review,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                )

                AnimatedVisibility(
                    modifier = Modifier.fillMaxWidth(),
                    visible = expanded,
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        FilledIconButton(onClick = { openLink(album.wikipediaUrl) }) {
                            Icon(painterResource(id = uiR.drawable.ic_wiki), contentDescription = null)
                        }

                        StreamingServices.from(album).getStreamingLinkFor(preferredStreamingPlatform)?.let {
                            FilledIconButton(onClick = { openLink(it) }) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            }
                        }
                    }
                }
            }
            val rating = when (val rating = historicAlbum.rating) {
                is Rating.Rated -> rating.rating.toString()
                Rating.Unrated -> stringResource(id = R.string.rating_unrated)
                Rating.DidNotListen -> stringResource(id = R.string.rating_did_not_listen)
            }
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterVertically),
                text = rating,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Preview
@Composable
private fun FilledCard() {
    OagTheme {
        Column {
            HistoricAlbumCard(
                historicAlbum = historicAlbumPreviewData(),
                expanded = true,
                preferredStreamingPlatform = StreamingPlatform.Tidal,
                openLink = {},
                onClick = {},
            )
        }
    }
}
