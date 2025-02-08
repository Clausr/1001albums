package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.android.require
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.feature.overview.navigation.OverviewDirections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val albumSlug by savedStateHandle.require<String>(OverviewDirections.Args.ALBUM_SLUG)
    val listName = savedStateHandle.get<String>(OverviewDirections.Args.LIST_NAME)

    val state = combine(
        oagRepository.getHistoricAlbum(albumSlug),
        oagRepository.preferredStreamingPlatform,
    ) { historicAlbum, streaming ->
        AlbumDetailsViewState(
            album = historicAlbum,
            streamingPlatform = streaming,
            relatedAlbums = getRelatedAlbums(
                artist = historicAlbum.album.artist,
                generatedAt = historicAlbum.metadata?.generatedAt
            ),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlbumDetailsViewState(),
        )

    private suspend fun getRelatedAlbums(
        artist: String,
        generatedAt: Instant?,
    ): ImmutableList<HistoricAlbum> {
        return oagRepository.getSimilarAlbums(artist)
            .filterNot { it.album.slug == albumSlug && it.metadata?.generatedAt == generatedAt }
            .toPersistentList()
    }

    init {
        viewModelScope.launch {
            getAlbumReviews()
        }
    }

    private suspend fun getAlbumReviews() {
        oagRepository.getAlbumReviews(albumSlug)
    }

    data class AlbumDetailsViewState(
        val album: HistoricAlbum? = null,
        val streamingPlatform: StreamingPlatform = StreamingPlatform.Undefined,
        val relatedAlbums: ImmutableList<HistoricAlbum> = persistentListOf(),
    )
}
