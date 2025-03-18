package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.android.require
import dk.clausr.core.data.repository.AlbumReviewRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.GroupReview
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.feature.overview.navigation.OverviewDirections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    private val albumReviewRepository: AlbumReviewRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val listName = savedStateHandle.get<String>(OverviewDirections.Args.LIST_NAME)
    private val albumId by savedStateHandle.require<String>(OverviewDirections.Args.ALBUM_ID)

    private val reviewState = albumReviewRepository.getGroupReviews(albumId)
        .onStart { AlbumReviewsViewState.Loading }
        .map {
            if (it.isEmpty()) {
                AlbumReviewsViewState.None
            } else {
                AlbumReviewsViewState.Success(it)
            }
        }
        .catch {
            AlbumReviewsViewState.Failed(NetworkError.TooManyRequests(it))
        }

    val state = combine(
        oagRepository.getHistoricAlbum(albumId),
        oagRepository.preferredStreamingPlatform,
        reviewState,
    ) { historicAlbum, streaming, reviewState ->
        AlbumDetailsViewState(
            album = historicAlbum,
            streamingPlatform = streaming,
            relatedAlbums = getRelatedAlbums(
                artist = historicAlbum.album.artist,
                generatedAt = historicAlbum.metadata?.generatedAt
            ),
            reviewViewState = reviewState,
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
            .filterNot { it.album.id == albumId && it.metadata?.generatedAt == generatedAt }
            .toPersistentList()
    }

    data class AlbumDetailsViewState(
        val album: HistoricAlbum? = null,
        val streamingPlatform: StreamingPlatform = StreamingPlatform.Undefined,
        val reviewViewState: AlbumReviewsViewState = AlbumReviewsViewState.None,
        val relatedAlbums: ImmutableList<HistoricAlbum> = persistentListOf(),
    )

    sealed interface AlbumReviewsViewState {
        data object Loading : AlbumReviewsViewState
        data object None : AlbumReviewsViewState // Not in a group
        data class Success(val reviews: List<GroupReview>) : AlbumReviewsViewState
        data class Failed(val error: NetworkError) : AlbumReviewsViewState
    }
}
