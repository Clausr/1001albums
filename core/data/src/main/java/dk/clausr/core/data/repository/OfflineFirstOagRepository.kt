package dk.clausr.core.data.repository

import androidx.datastore.core.DataStore
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.common.model.Result
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.common.model.map
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data.model.toAlbumImageEntities
import dk.clausr.core.data.model.toEntity
import dk.clausr.core.data.model.toHistoricAlbum
import dk.clausr.core.data.model.toRatingEntity
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectId
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.AlbumImageDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.RatingDao
import dk.clausr.core.database.model.AlbumEntity
import dk.clausr.core.database.model.AlbumImageEntity
import dk.clausr.core.database.model.RatingEntity
import dk.clausr.core.model.Album
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.measureTimedValue

@Suppress("LongParameterList")
@Singleton
class OfflineFirstOagRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val widgetDataStore: DataStore<SerializedWidgetState>,
    private val albumDao: AlbumDao,
    private val projectDao: ProjectDao,
    private val ratingDao: RatingDao,
    private val albumImageDao: AlbumImageDao,
) : OagRepository {
    override val widgetState = widgetDataStore.data.map {
        it
    }

    override val projectId: Flow<String?> = widgetDataStore.data.map {
        Timber.d("ProjectId: $it")
        it.projectId
    }

    override val project: Flow<Project?> =
        combine(projectDao.getProject(), albumDao.getAlbums()) { project, albums ->
            val (value, time) = measureTimedValue {

                val history = albums.map { it.asExternalModel() }
                val ratings = ratingDao.getRatingByAlbumSlugs(albums.map { it.slug })

                val historicAlbums = ratings.map { rating ->
                    rating.toHistoricAlbum(history.first { it.slug == rating.albumSlug })
                }

                project?.asExternalModel(historicAlbums.sortedByDescending { it.generatedAt })
            }
            Timber.d("Time of getting project.. $time")
            value
        }
//        .map { it?.asExternalModel() }

    override val currentAlbum: Flow<Album?> = project
        .mapNotNull { project ->
            project?.currentAlbumSlug?.let { currentSlug ->
                albumDao.getAlbumBySlug(currentSlug)?.asExternalModel()
            }
        }

    override val historicAlbums: Flow<List<HistoricAlbum>> = albumDao.getAlbums()
        .map { albums ->
            albums.mapNotNull { albumEntity ->
                val rating = ratingDao.getRatingByAlbumSlug(albumSlug = albumEntity.slug)
                val album = albumEntity.asExternalModel()

                rating?.toHistoricAlbum(album)
            }
                .sortedByDescending { it.generatedAt }
        }


    override suspend fun setProject(projectId: String) = withContext(ioDispatcher) {
        Timber.d("Set new project $projectId")
        widgetDataStore.updateData { SerializedWidgetState.Loading(projectId) }
        projectDao.clearTable()
        albumDao.clearTable()
        ratingDao.clearTable()

        getAndUpdateProject(projectId)

        Unit
    }

    private suspend fun putNetworkProjectIntoDatabase(networkProject: NetworkProject) {
        Timber.d("Put network project ${networkProject.name} into database")

        // Insert project into DB
        projectDao.insertProject(networkProject.toEntity())

        // Insert current album into DB
        with(networkProject.currentAlbum) {
            albumDao.insert(this.toEntity())
            albumImageDao.insertAll(this.toAlbumImageEntities())
        }

        // Insert albums with ratings into DB
        val albumEntities = mutableListOf<AlbumEntity>()
        val ratingEntities = mutableListOf<RatingEntity>()
        val albumImageEntities = mutableListOf<AlbumImageEntity>()

        for (historicAlbum in networkProject.history) {
            albumEntities.add(historicAlbum.album.toEntity())
            albumImageEntities.addAll(historicAlbum.album.toAlbumImageEntities())
            ratingEntities.add(historicAlbum.toRatingEntity())
        }
        albumDao.insertAlbums(albumEntities)
        ratingDao.insertRatings(ratingEntities)
        albumImageDao.insertAll(albumImageEntities)
    }

    private suspend fun getAndUpdateProject(projectId: String): Result<Project> =
        withContext(ioDispatcher) {
            networkDataSource.getProject(projectId)
                .doOnSuccess { networkProject ->
                    Timber.d("Got project ${networkProject.name} with ${networkProject.history.size} albums")
                    putNetworkProjectIntoDatabase(networkProject)

                    // Update widget
                    updateWidgetData(
                        project = networkProject.asExternalModel(),
                        currentAlbum = networkProject.currentAlbum.asExternalModel(),
                        historicAlbums = networkProject.history.map { it.asExternalModel() }
                    )
                }
                .doOnFailure { message, throwable ->
                    Timber.e(throwable, message ?: "Project failure")
                }
                .map {
                    it.asExternalModel()
                }
        }

    override suspend fun isLatestAlbumRated(): Boolean {
        val history = historicAlbums.firstOrNull()
        val lastAlbum = history?.firstOrNull()

        Timber.d("isLatestAlbumRated ${lastAlbum?.album?.name} -- rating: ${lastAlbum?.rating}")

        return lastAlbum?.rating is Rating.Rated
    }

    private suspend fun updateWidgetData(
        project: Project,
        currentAlbum: Album,
        historicAlbums: List<HistoricAlbum>,
    ) {
        Timber.d("Update widget data")
        widgetDataStore.updateData { _ ->
            // Find the index of the latest rated album
            val lastRatedIndex = historicAlbums.indexOfLast { it.rating is Rating.Rated }
            // Find the first item without a rating after the last rated
            val firstUnratedAlbum =
                historicAlbums.drop(lastRatedIndex + 1).firstOrNull { it.rating is Rating.Unrated }

            Timber.d("First unrated: ${firstUnratedAlbum?.album?.name}")

            val albumToUse = firstUnratedAlbum?.album ?: currentAlbum

            SerializedWidgetState.Success(
                data = AlbumWidgetData(
                    coverUrl = albumToUse.imageUrl,
                    newAvailable = firstUnratedAlbum != null,
                    wikiLink = albumToUse.wikipediaUrl,
                    streamingServices = StreamingServices.from(albumToUse),
                ),
                currentProjectId = project.name
            )
        }
    }

    override suspend fun setPreferredPlatform(platform: StreamingPlatform) {
        widgetDataStore.updateData { oldData ->
            if (oldData is SerializedWidgetState.Success) {
                oldData.copy(data = oldData.data.copy(preferredStreamingPlatform = platform))
            } else {
                oldData
            }
        }
    }

    override suspend fun updateProject(projectId: String): Result<Project> {
        return getAndUpdateProject(projectId)
    }
}
