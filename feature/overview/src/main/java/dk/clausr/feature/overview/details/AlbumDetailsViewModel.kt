package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.android.require
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val isError: MutableStateFlow<NetworkError?> = MutableStateFlow(null)
    private val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val _reviewState: MutableStateFlow<AlbumReviewsViewState> = MutableStateFlow(AlbumReviewsViewState.None)
    private val reviewState: Flow<AlbumReviewsViewState> = _reviewState

    private suspend fun getAlbumReviewsFromNetwork() {
        isLoading.emit(true)
        oagRepository.getAlbumReviews(albumId)
            .doOnSuccess {
                isError.emit(null)
                isLoading.emit(false)

                _reviewState.emit(AlbumReviewsViewState.Success(it))
            }
            .doOnFailure {
                if (it is NetworkError.NoGroup) {

                    isError.emit(null)
                    isLoading.emit(false)
                    _reviewState.emit(AlbumReviewsViewState.None)
                    return
                }
                // Don't override cached response with an error
                if (_reviewState.value !is AlbumReviewsViewState.Success) {
                    isError.emit(it)
                    _reviewState.emit(AlbumReviewsViewState.Failed(it))
                }

                if (it is NetworkError.TooManyRequests && maxRetries < retries) {
                    delay(20.seconds)
                    retries++
                    getAlbumReviewsFromNetwork()
                } else {
                    isLoading.emit(false)
                }
            }
    }

    init {
        // Query backend
        viewModelScope.launch {
            getAlbumReviewsFromNetwork()
        }

        // Listen for DAO changes...
        viewModelScope.launch {
            oagRepository.getReviewsFromDatabase(albumId)
                .collect {
                    _reviewState.emit(
                        if (it.isEmpty()) {
                            AlbumReviewsViewState.None
                        } else {
                            AlbumReviewsViewState.Success(it)
                        }
                    )
                }
        }
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
