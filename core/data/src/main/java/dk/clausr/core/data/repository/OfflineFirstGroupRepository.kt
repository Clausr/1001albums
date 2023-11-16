package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data.model.toEntity
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.WidgetDao
import dk.clausr.core.database.model.WidgetEntity
import dk.clausr.core.database.model.asExternalModel
import dk.clausr.core.datastore.OagDataStore
import dk.clausr.core.model.Album
import dk.clausr.core.model.OAGWidget
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OfflineFirstGroupRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val dataStore: OagDataStore,
    private val projectDao: ProjectDao,
    private val widgetDao: WidgetDao,
    private val albumDao: AlbumDao
) : OagRepository {
    override val projectId: Flow<String?> = dataStore.projectId

    override val widget = projectId.mapNotNull { it }
        .flatMapLatest {
            widgetDao.getWidgetFlow(it).map { it?.asExternalModel() }
        }

    override suspend fun getWidget(projectId: String): OAGWidget? = withContext(ioDispatcher) {
        widgetDao.getWidget(projectId)?.asExternalModel()
    }

    override suspend fun setProject(projectId: String) {
        dataStore.setProject(projectId)

        updateDailyAlbum(projectId)
    }

    override suspend fun updateDailyAlbum(projectId: String) {
        val projectRes = networkDataSource.getProject(projectId).getOrThrow()
        val project = projectRes?.asExternalModel() ?: return

        projectDao.insertProject(projectRes.toEntity())

        albumDao.insertAlbums(projectRes.history.map {
            it.album.toEntity()
        })
        albumDao.insert(projectRes.currentAlbum.toEntity())

        val lastAlbum = project.history.lastOrNull()

        Timber.d("Update daily album: ${project.currentAlbum.artist} -- yesterdays: $lastAlbum")
        val widget = when (lastAlbum?.rating) {
            Rating.Unrated -> {
                WidgetEntity(
                    projectName = projectId,
                    currentAlbumTitle = lastAlbum.album.name,
                    currentAlbumArtist = lastAlbum.album.artist,
                    currentCoverUrl = lastAlbum.album.images.maxBy { it.height }.url,
                    newAlbumAvailable = true
                )
            }

            else -> {
                WidgetEntity(
                    projectName = project.name,
                    currentAlbumTitle = project.currentAlbum.name,
                    currentAlbumArtist = project.currentAlbum.artist,
                    currentCoverUrl = project.currentAlbum.images.maxBy { it.height }.url,
                    newAlbumAvailable = false
                )
            }
        }

        widgetDao.insert(widget)
    }

    override val project: Flow<Project?> = projectId
        .mapNotNull { it }
        .flatMapLatest {
            projectDao.getProject(it)
                .map {
                    it?.asExternalModel()
                }
        }

    override val albums: Flow<List<Album>> = albumDao.getAlbums()
        .map { albums ->
            albums.map { album ->
                album.asExternalModel()
            }
        }

    override suspend fun getProject(projectId: String): Project? {
        val projectRes = networkDataSource.getProject(projectId).getOrThrow()

        return projectRes?.asExternalModel()
    }
}
