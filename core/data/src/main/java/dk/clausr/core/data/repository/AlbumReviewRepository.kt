package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbumGroupReviews
import dk.clausr.core.common.model.Result
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.ReviewData
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data.model.mapToHistoricAlbum
import dk.clausr.core.data.model.toEntity
import dk.clausr.core.database.dao.AlbumWithOptionalRatingDao
import dk.clausr.core.database.dao.GroupReviewDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.RatingDao
import dk.clausr.core.model.GroupReview
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class AlbumReviewRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val groupReviewDao: GroupReviewDao,
    private val albumWithOptionalRatingDao: AlbumWithOptionalRatingDao,
    private val ratingDao: RatingDao,
    private val projectDao: ProjectDao,
) {
    fun getGroupReviews(albumId: String): Flow<ReviewData> = flow {
        val groupId = projectDao.getGroupId()
        val projectId = projectDao.getProjectId()

        val personalReview = listOfNotNull(
            getPersonalReview(
                projectId = projectId,
                albumId = albumId,
            ),
        )

        // Get initial cached reviews
        val cachedReviews = groupReviewDao.getReviewsFor(albumId).map { it.asExternalModel() }

        // Don't show loading if we're in a group and we already have some reviews
        val showLoading = groupId != null && cachedReviews.size <= 1

        // Emit cached reviews first; fallback to personalReviews if empty
        emit(
            ReviewData(
                reviews = cachedReviews.ifEmpty { personalReview },
                isLoading = showLoading, // Show loading state since network fetch will begin
            ),
        )

        emitAll(
            groupReviewDao.getReviewsForFlow(albumId)
                .onStart {
                    Timber.v("flowOnStart - Get network reviews")
                    // Trigger network refresh async
                    groupId?.let {
                        retryNetworkCall {
                            networkDataSource.getGroupReviewsForAlbum(it, albumId)
                                .doOnSuccess { reviews ->
                                    groupReviewDao.insert(reviews.toEntity(albumId))
                                    persistOwnRating(albumId, projectId, reviews)
                                }
                        }
                        Unit
                    }
                }
                .map { dbGroupReviews ->
                    ReviewData(
                        reviews = dbGroupReviews.map { it.asExternalModel() }.ifEmpty { personalReview },
                        isLoading = true,
                    )
                }
                .mapLatest { cachedData ->
                    cachedData.copy(isLoading = false)
                },
        )
    }.flowOn(ioDispatcher)

    /**
     * Mirror the user's own review from the group-reviews response into the local `ratings` row so the
     * reactive Flows behind the details screen and the home "Did not listen" list reflect a rating made
     * on the external website — without a full project sync, reusing the call we already make here.
     *
     * ponytail: group-only. Solo projects have no per-album endpoint, so their rating still lands via
     * the next full project sync (pull-to-refresh / worker).
     */
    private suspend fun persistOwnRating(
        albumId: String,
        projectId: String,
        reviews: NetworkAlbumGroupReviews,
    ) {
        val mine = reviews.reviews.firstOrNull { it.projectName == projectId } ?: return
        val existing = albumWithOptionalRatingDao.getAlbumById(albumId).rating ?: return
        if (existing.rating == mine.rating && existing.review == mine.review.orEmpty()) return
        ratingDao.insertRatings(listOf(existing.copy(rating = mine.rating, review = mine.review.orEmpty())))
    }

    fun getPersonalReview(
        projectId: String,
        albumId: String,
    ): GroupReview? {
        return albumWithOptionalRatingDao.getAlbumById(id = albumId).mapToHistoricAlbum().metadata?.let { metadata ->
            GroupReview(
                author = projectId,
                rating = metadata.rating,
                review = metadata.review,
            )
        }
    }

    fun getPersonalReviewFlow(albumId: String): Flow<GroupReview?> = flow<GroupReview?> {
        val projectId = projectDao.getProjectId()

        val metadata = albumWithOptionalRatingDao.getAlbumById(id = albumId).mapToHistoricAlbum().metadata

        emit(
            GroupReview(
                author = projectId,
                rating = metadata?.rating,
                review = metadata?.review,
            ),
        )
    }
        .flowOn(ioDispatcher)

    @Throws(IllegalStateException::class)
    private suspend fun <T, E> retryNetworkCall(networkCall: suspend () -> Result<T, E>): T {
        var retries = 0
        val maxRetries = 3
        val delayBetweenRetries = 5.seconds

        while (retries < maxRetries) {
            when (val result = networkCall()) {
                is Result.Success -> {
                    // Exit the loop and return the value if successful
                    return result.value
                }

                is Result.Failure -> {
                    // Log or handle the failure (optional)
                    retries++
                    if (retries >= maxRetries) {
                        // Throw an exception or propagate the failure when retries are exhausted
                        throw result.throwable ?: Exception("Network error: ${result.reason}")
                    }
                    delay(delayBetweenRetries) // Wait before retrying
                }
            }
        }

        error("Retries exhausted") // Failsafe, should not reach here
    }
}
