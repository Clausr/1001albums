package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.android.require
import dk.clausr.core.common.model.Result
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.GroupReview
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.feature.overview.navigation.OverviewDirections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val listName = savedStateHandle.get<String>(OverviewDirections.Args.LIST_NAME)

    private val albumId by savedStateHandle.require<String>(OverviewDirections.Args.ALBUM_ID)
    private var retries = 0
    private val maxRetries = 5

    private val reviewViewState: StateFlow<AlbumReviewsViewState> = oagRepository.getAlbumReviews2(albumId)
        .onStart {
            AlbumReviewsViewState.Loading
        }
        .map {
            when (it) {
                is Result.Failure -> {
                    if (it.reason is NetworkError.NoGroup) {
                        Timber.e(it.reason.cause, "No group")
                        AlbumReviewsViewState.None
                    } else {
                        if (it.reason is NetworkError.TooManyRequests) {
                            Timber.d("viewModelScope.isActive ${viewModelScope.isActive}")
                            delay(20.seconds)
                            if (viewModelScope.isActive && retries <= maxRetries) {
                                retries += 1
                                Timber.i("Retrying for reviews: $retries")
//                                getAlbumReviews()
                            }
                        }
                        AlbumReviewsViewState.Failed(it.reason)
                    }
                }

                is Result.Success -> {
                    AlbumReviewsViewState.Success(it.value)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlbumReviewsViewState.None,
        )

    val state = combine(
        oagRepository.getHistoricAlbum(albumId),
        oagRepository.preferredStreamingPlatform,
        reviewViewState,
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
