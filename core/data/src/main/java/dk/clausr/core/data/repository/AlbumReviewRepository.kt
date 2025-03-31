package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
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

        // Emit cached reviews first; fallback to personalReviews if empty
        emit(
            ReviewData(
                reviews = cachedReviews.ifEmpty { personalReview },
                isLoading = true, // Show loading state since network fetch will begin
            ),
        )

        emitAll(
            groupReviewDao.getReviewsForFlow(albumId)
                .map { dbGroupReviews ->
                    Timber.v("emit dbReviews: ${dbGroupReviews.size}")
                    ReviewData(
                        reviews = dbGroupReviews.map { it.asExternalModel() }.ifEmpty { personalReview },
                        isLoading = true,
                    )
                }
                .onStart {
                    Timber.v("flowOnStart - Get network reviews")
                    // Trigger network refresh async
                    groupId?.let {
                        val test = retryNetworkCall {
                            networkDataSource.getGroupReviewsForAlbum(it, albumId)
                                .doOnSuccess { reviews ->
                                    groupReviewDao.insert(reviews.toEntity(albumId))
                                }
                        }
                        Timber.v("Network responded with ${test.reviews.size} reviews")
                    }
                }
                .mapLatest { cachedData ->
                    Timber.v("mapLatest - Set loading to false")
                    cachedData.copy(isLoading = false)
                },
        )
    }.flowOn(ioDispatcher)

    private fun getPersonalReview(
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
