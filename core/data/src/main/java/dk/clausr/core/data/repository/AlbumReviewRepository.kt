package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.model.Result
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
    fun getGroupReviews(albumId: String): Flow<ReviewData> {
        return flow {
            val groupId = projectDao.getGroupId()
            val projectId = projectDao.getProjectId()

            val personalReview = listOfNotNull(
                getPersonalReview(
                    projectId = projectId,
                    albumId = albumId,
                ),
            )
            val localReviews = if (groupId == null) {
                personalReview
            } else {
                groupReviewDao.getReviewsFor(albumId).map {
                    it.asExternalModel()
                }
            }

            emit(
                ReviewData(
                    reviews = localReviews,
                    isLoading = localReviews.size <= 1 && groupId != null,
                ),
            )

            val networkReviews = if (groupId != null) {
                val networkResponse = retryNetworkCall {
                    networkDataSource.getGroupReviewsForAlbum(groupId, albumId)
                }

                groupReviewDao.insert(networkResponse.toEntity(albumId))

                networkResponse.asExternalModel()
            } else {
                localReviews
            }

            emit(
                ReviewData(
                    reviews = networkReviews,
                    isLoading = false,
                ),
            )
        }.flowOn(ioDispatcher)
    }

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
