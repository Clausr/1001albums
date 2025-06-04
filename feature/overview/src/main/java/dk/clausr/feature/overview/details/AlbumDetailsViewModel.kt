package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.ExternalLinks
import dk.clausr.core.data.repository.AlbumReviewRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.GroupReview
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.feature.overview.navigation.AlbumDetailsRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    albumReviewRepository: AlbumReviewRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val navArgs: AlbumDetailsRoute = savedStateHandle.toRoute<AlbumDetailsRoute>()
    val listName = navArgs.listName
    private val albumId = navArgs.albumId

    private val _historyLink = oagRepository.projectId
        .map {
            val projectId = it ?: return@map null
            ExternalLinks.Generator.historyLink(
                projectId = projectId,
                albumId = albumId,
            )
        }

    private val reviewState = albumReviewRepository.getGroupReviews(albumId)
        .map {
            if (it.reviews.isEmpty() && it.isLoading) {
                AlbumReviewsViewState.Loading
            } else if (it.reviews.isEmpty()) {
                AlbumReviewsViewState.None
            } else {
                AlbumReviewsViewState.Success(
                    reviews = it.reviews,
                    loading = it.isLoading,
                )
            }
        }
        .catch {
            AlbumReviewsViewState.Failed(NetworkError.TooManyRequests(it))
        }

    val state = combine(
        oagRepository.getHistoricAlbum(albumId),
        oagRepository.preferredStreamingPlatform,
        reviewState,
        _historyLink,
    ) { historicAlbum, streaming, reviewState, historyLink ->
        AlbumDetailsViewState(
            album = historicAlbum,
            streamingPlatform = streaming,
            relatedAlbums = getRelatedAlbums(
                artist = historicAlbum.album.artist,
                generatedAt = historicAlbum.metadata?.generatedAt,
            ),
            reviewViewState = reviewState,
            historyLink = historyLink,
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
        val historyLink: String? = null,
    )

    sealed interface AlbumReviewsViewState {
        data object Loading : AlbumReviewsViewState
        data object None : AlbumReviewsViewState // Not in a group
        data class Success(
            val reviews: List<GroupReview>,
            val loading: Boolean = false,
        ) : AlbumReviewsViewState

        data class Failed(val error: NetworkError) : AlbumReviewsViewState
    }
}
