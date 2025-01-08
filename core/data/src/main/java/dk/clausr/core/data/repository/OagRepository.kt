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
import dk.clausr.core.data.model.mapToHistoricAlbum
import dk.clausr.core.data.model.toAlbumImageEntities
import dk.clausr.core.data.model.toEntity
import dk.clausr.core.data.model.toRatingEntity
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectId
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.AlbumImageDao
import dk.clausr.core.database.dao.AlbumWithOptionalRatingDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.RatingDao
import dk.clausr.core.database.model.AlbumEntity
import dk.clausr.core.database.model.AlbumImageEntity
import dk.clausr.core.database.model.AlbumWithOptionalRating
import dk.clausr.core.database.model.RatingEntity
import dk.clausr.core.model.Album
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.core.network.NetworkError
import dk.clausr.core.ui.CoverData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("LongParameterList")
@Singleton
class OagRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val widgetDataStore: DataStore<SerializedWidgetState>,
    private val albumDao: AlbumDao,
    private val projectDao: ProjectDao,
    private val ratingDao: RatingDao,
    private val albumImageDao: AlbumImageDao,
    private val albumWithOptionalRatingDao: AlbumWithOptionalRatingDao,
) {
    val widgetState = widgetDataStore.data

    val projectId: Flow<String?> = widgetDataStore.data.map {
        it.projectId
    }.distinctUntilChanged()

    val preferredStreamingPlatform: Flow<StreamingPlatform> = widgetDataStore.data.map {
        when (it) {
            is SerializedWidgetState.Success -> it.data.preferredStreamingPlatform
            is SerializedWidgetState.Error -> StreamingPlatform.Undefined
            is SerializedWidgetState.Loading -> StreamingPlatform.Undefined
            SerializedWidgetState.NotInitialized -> StreamingPlatform.Undefined
        }
    }

    private val historicAlbums: Flow<List<HistoricAlbum>> =
        albumWithOptionalRatingDao.getAlbumsWithRatings().map { it.map(AlbumWithOptionalRating::mapToHistoricAlbum) }

    val project: Flow<Project?> = combine(
        projectDao.getProject(),
        historicAlbums,
    ) { project, historicAlbums ->
        project?.asExternalModel(historicAlbums)
    }.distinctUntilChanged()

    val currentAlbum: Flow<Album?> = project.mapNotNull { project ->
        project?.historicAlbums?.lastRevealedUnratedAlbum()?.album ?: project?.currentAlbumSlug?.let { currentSlug ->
            albumWithOptionalRatingDao.getAlbumBySlug(currentSlug)?.album?.asExternalModel()
        }
    }


    val didNotListenAlbums: Flow<List<HistoricAlbum>> =
        albumWithOptionalRatingDao.getDidNotListenAlbums().map { it.map(AlbumWithOptionalRating::mapToHistoricAlbum) }

    suspend fun setProject(projectId: String): Result<Project, NetworkError> {
        Timber.d("Set new project $projectId")

        return networkDataSource.getProject(projectId)
            .doOnSuccess { networkProject ->
                widgetDataStore.updateData { oldData ->
                    val oldPreferredStreamingPlatform =
                        (oldData as? SerializedWidgetState.Success)?.data?.preferredStreamingPlatform ?: StreamingPlatform.Undefined
                    SerializedWidgetState.Loading(projectId, oldPreferredStreamingPlatform)
                }
                projectDao.clearTable()
                albumDao.clearTable()
                ratingDao.clearTable()

                putNetworkProjectIntoDatabase(networkProject)

                // Update widget
                updateWidgetData(
                    project = networkProject.asExternalModel(),
                    currentAlbum = networkProject.currentAlbum.asExternalModel(),
                    historicAlbums = networkProject.history.map { it.asExternalModel() },
                )
            }
            .doOnFailure {
                Timber.w("Could not set new project")
            }
            .map(NetworkProject::asExternalModel)
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

    private suspend fun getAndUpdateProject(projectId: String): Result<Project, NetworkError> = withContext(ioDispatcher) {
        networkDataSource.getProject(projectId)
            .doOnSuccess { networkProject ->
                Timber.d("Got project ${networkProject.name} with ${networkProject.history.size} albums")
                putNetworkProjectIntoDatabase(networkProject)

                // Update widget
                updateWidgetData(
                    project = networkProject.asExternalModel(),
                    currentAlbum = networkProject.currentAlbum.asExternalModel(),
                    historicAlbums = networkProject.history.map { it.asExternalModel() },
                )
            }
            .doOnFailure { error ->
                Timber.e(error.cause, "Could not getAndUpdate project ${error.cause}")
            }
            .map {
                it.asExternalModel()
            }
    }

    suspend fun isLatestAlbumRated(): Boolean {
        val latestRevealedAlbum = albumWithOptionalRatingDao.getLatestRevealedAlbum()?.mapToHistoricAlbum()

        val isLastAlbumRated = when (latestRevealedAlbum?.metadata?.rating) {
            Rating.DidNotListen -> true
            is Rating.Rated -> true
            Rating.Unrated -> false
            null -> false
        }

        Timber.d("isLatestRevealedAlbum Rated $isLastAlbumRated")

        return isLastAlbumRated
    }

    private suspend fun updateWidgetData(
        project: Project,
        currentAlbum: Album,
        historicAlbums: List<HistoricAlbum>,
    ) {
        widgetDataStore.updateData { old ->
            val lastRevealedUnratedAlbum = historicAlbums.lastRevealedUnratedAlbum()
            val albumToUse = lastRevealedUnratedAlbum?.album ?: currentAlbum

            val oldPreferredPlatform = when (old) {
                is SerializedWidgetState.Loading -> old.previousStreamingPlatform
                is SerializedWidgetState.Success -> old.data.preferredStreamingPlatform
                is SerializedWidgetState.Error -> StreamingPlatform.Undefined
                SerializedWidgetState.NotInitialized -> StreamingPlatform.Undefined
            }

            val oldNotifications = when (old) {
                is SerializedWidgetState.Success -> old.data.unreadNotifications
                else -> 0
            }

            SerializedWidgetState.Success(
                data = AlbumWidgetData(
                    newAvailable = lastRevealedUnratedAlbum != null,
                    coverUrl = albumToUse.imageUrl,
                    wikiLink = albumToUse.wikipediaUrl,
                    streamingServices = StreamingServices.from(albumToUse),
                    preferredStreamingPlatform = oldPreferredPlatform,
                    unreadNotifications = oldNotifications,
                ),
                currentProjectId = project.name,
            )
        }
    }

    /**
     * Album to show is defined as follow:
     * currentAlbum is always displayed UNLESS:
     * isRevealed == true AND rating == unrated
     */
    private fun List<HistoricAlbum>.lastRevealedUnratedAlbum(): HistoricAlbum? {
        return this.reversed().firstOrNull { it.metadata?.isRevealed == true && it.metadata?.rating is Rating.Unrated }
    }

    suspend fun setPreferredPlatform(platform: StreamingPlatform) {
        widgetDataStore.updateData { oldData ->
            if (oldData is SerializedWidgetState.Success) {
                oldData.copy(data = oldData.data.copy(preferredStreamingPlatform = platform))
            } else {
                oldData
            }
        }
    }

    suspend fun updateProject(projectId: String): Result<Project, NetworkError> {
        return getAndUpdateProject(projectId)
    }

    val albumCovers: Flow<CoverData> = albumImageDao.getAlbumCovers().map {
        CoverData.createCoverDataOrDefault(externalList = it)
    }

    fun getHistoricAlbum(slug: String): Flow<HistoricAlbum> = albumWithOptionalRatingDao
        .getAlbumBySlugFlow(slug)
        .map(AlbumWithOptionalRating::mapToHistoricAlbum)

    suspend fun getSimilarAlbums(artist: String): List<HistoricAlbum> = withContext(ioDispatcher) {
        albumWithOptionalRatingDao.getSimilarAlbumsWithRatings(artist).map(AlbumWithOptionalRating::mapToHistoricAlbum)
    }
}
