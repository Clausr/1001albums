package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.android.require
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.AlbumGroupReviews
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.feature.overview.navigation.OverviewDirections
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val albumId by savedStateHandle.require<String>(OverviewDirections.Args.ALBUM_ID)
    val listName = savedStateHandle.get<String>(OverviewDirections.Args.LIST_NAME)

    // TODO This needs a loading state / error state
    private val reviews = MutableStateFlow<AlbumGroupReviews?>(null)

    private val reviewViewState = MutableStateFlow<AlbumReviewsViewState>(AlbumReviewsViewState.None)

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

    init {
        viewModelScope.launch {
            getAlbumReviews()
        }
    }

    private var retries = 0
    private val maxRetries = 5

    private suspend fun getAlbumReviews() {
        Timber.d("viewModelScope.isActive ${viewModelScope.isActive}")
        reviewViewState.emit(AlbumReviewsViewState.Loading)
        oagRepository.getAlbumReviews(albumId = albumId)
            .doOnSuccess {
                reviewViewState.emit(AlbumReviewsViewState.Success(it.reviews))
                reviews.emit(it)
                retries = 0
            }
            .doOnFailure {
                if (it is NetworkError.NoGroup) {
                    reviewViewState.emit(AlbumReviewsViewState.None)
                    Timber.e(it.cause, "No group")
                } else {
                    reviewViewState.emit(AlbumReviewsViewState.Failed(it))
                    if (it is NetworkError.TooManyRequests) {
                        Timber.d("viewModelScope.isActive ${viewModelScope.isActive}")
                        delay(20.seconds)
                        if (viewModelScope.isActive && retries <= maxRetries) {
                            retries += 1
                            getAlbumReviews()
                        }
                    }
                }
            }
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
        data class Success(val reviews: List<AlbumGroupReviews.GroupReview>) : AlbumReviewsViewState
        data class Failed(val error: NetworkError) : AlbumReviewsViewState
    }
}
