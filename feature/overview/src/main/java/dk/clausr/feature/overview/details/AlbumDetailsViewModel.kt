package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.ExternalLinks
import dk.clausr.core.data.repository.AlbumReviewRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data.repository.UserRepository
import dk.clausr.core.model.GroupReview
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.feature.overview.navigation.AlbumDetailsRoute
import dk.clausr.feature.overview.navigation.AlbumListSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Identifies an entry in a list, as the same album can be generated more than once. */
data class AlbumKey(
    val id: String,
    val generatedAt: Long?,
)

val HistoricAlbum.key: AlbumKey
    get() = AlbumKey(id = album.id, generatedAt = metadata?.generatedAt?.toEpochMilli())

// Lets the user swipe past albums without fetching reviews for each of them
private val ReviewsNetworkDelay = 400.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    private val albumReviewRepository: AlbumReviewRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val navArgs: AlbumDetailsRoute = savedStateHandle.toRoute<AlbumDetailsRoute>()
    val listName = navArgs.listName
    val listSource = navArgs.source

    private var swipeHintConsumed = false

    private val selectedKey = MutableStateFlow(AlbumKey(id = navArgs.albumId, generatedAt = navArgs.generatedAt))

    /** The albums of the section the details were opened from, in the order they're shown there. */
    private val listAlbums: Flow<List<HistoricAlbum>> = when (navArgs.source) {
        AlbumListSource.History -> oagRepository.historicAlbums.map { albums ->
            albums.filter { it.metadata != null }.inSelectedTab()
        }

        AlbumListSource.DidNotListen -> oagRepository.didNotListenAlbums.map { it.inSelectedTab() }
        AlbumListSource.TopRated -> oagRepository.topRatedAlbums.map { it.inSelectedTab() }
        AlbumListSource.Related -> flow {
            val artist = oagRepository.getHistoricAlbum(navArgs.albumId).first().album.artist
            val relatedTo = AlbumKey(id = navArgs.relatedToAlbumId.orEmpty(), generatedAt = navArgs.relatedToGeneratedAt)
            emit(oagRepository.getSimilarAlbums(artist).filterNot { it.key == relatedTo })
        }

        AlbumListSource.None -> flowOf(emptyList())
    }

    private val selectedAlbumDetails: Flow<SelectedAlbumDetails> = selectedKey
        .withIndex()
        .flatMapLatest { (index, key) ->
            combine(
                oagRepository.getHistoricAlbum(key.id),
                reviewState(
                    albumId = key.id,
                    networkDelay = if (index == 0) Duration.ZERO else ReviewsNetworkDelay,
                ),
                externalRatingLink(key.id),
            ) { historicAlbum, reviewState, externalRatingLink ->
                SelectedAlbumDetails(
                    key = key,
                    album = historicAlbum,
                    reviewViewState = reviewState,
                    relatedAlbums = getRelatedAlbums(
                        artist = historicAlbum.album.artist,
                        exclude = key,
                    ),
                    historyLink = externalRatingLink,
                )
            }
        }

    val state = combine(
        listAlbums,
        selectedKey,
        oagRepository.preferredStreamingPlatform,
        selectedAlbumDetails,
    ) { listAlbums, selectedKey, streaming, details ->
        val pages = when {
            listAlbums.any { it.key == selectedKey } -> listAlbums
            // Not opened from a list, or the album has left it since (e.g. rated on the website)
            details.key == selectedKey -> listOf(details.album)
            else -> emptyList()
        }

        AlbumDetailsViewState(
            pages = pages.toPersistentList(),
            selectedIndex = pages.indexOfFirst { it.key == selectedKey }.coerceAtLeast(0),
            albumKey = details.key,
            album = details.album,
            streamingPlatform = streaming,
            relatedAlbums = details.relatedAlbums,
            reviewViewState = details.reviewViewState,
            historyLink = details.historyLink,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlbumDetailsViewState(),
        )

    fun onAlbumSettled(album: HistoricAlbum) {
        selectedKey.value = album.key
    }

    fun onAlbumSwiped() {
        viewModelScope.launch { userRepository.setHasSwipedAlbums() }
    }

    /** Whether to nudge the cover to show it can be swiped. At most once per screen, also across configuration changes. */
    suspend fun consumeSwipeHint(): Boolean {
        if (swipeHintConsumed) return false
        swipeHintConsumed = true
        return userRepository.consumeAlbumSwipeHint()
    }

    private fun List<HistoricAlbum>.inSelectedTab() = filter { it.album.isUserAlbum == navArgs.userAlbums }

    private fun externalRatingLink(albumId: String): Flow<String?> = albumReviewRepository.getPersonalReviewFlow(albumId)
        .map { personalReview ->
            ExternalLinks.Generator.historyLink(
                projectId = personalReview?.author.orEmpty(),
                albumId = albumId,
                groupId = albumReviewRepository.getGroupId(),
            ).takeIf { personalReview?.rating !is Rating.Rated }
        }

    private fun reviewState(
        albumId: String,
        networkDelay: Duration,
    ): Flow<AlbumReviewsViewState> = albumReviewRepository.getGroupReviews(albumId, networkDelay)
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
            emit(AlbumReviewsViewState.Failed(NetworkError.TooManyRequests(it)))
        }

    private suspend fun getRelatedAlbums(
        artist: String,
        exclude: AlbumKey,
    ): ImmutableList<HistoricAlbum> {
        return oagRepository.getSimilarAlbums(artist)
            .filterNot { it.key == exclude }
            .toPersistentList()
    }

    private data class SelectedAlbumDetails(
        val key: AlbumKey,
        val album: HistoricAlbum,
        val reviewViewState: AlbumReviewsViewState,
        val relatedAlbums: ImmutableList<HistoricAlbum>,
        val historyLink: String?,
    )

    /**
     * @param pages The albums the user can swipe between.
     * @param albumKey The album that [album] and the rest of the details belong to. Trails the selected page while it loads.
     */
    data class AlbumDetailsViewState(
        val pages: ImmutableList<HistoricAlbum> = persistentListOf(),
        val selectedIndex: Int = 0,
        val albumKey: AlbumKey? = null,
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
