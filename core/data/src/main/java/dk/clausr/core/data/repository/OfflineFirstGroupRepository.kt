package dk.clausr.core.data.repository

import androidx.datastore.core.DataStore
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data.model.toEntity
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Loading
import dk.clausr.core.data_widget.SerializedWidgetState.Success
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.WidgetDao
import dk.clausr.core.database.model.WidgetEntity
import dk.clausr.core.database.model.asExternalModel
import dk.clausr.core.datastore.OagDataStore
import dk.clausr.core.model.Album
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstGroupRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val dataStore: OagDataStore,
    private val projectDao: ProjectDao,
    private val widgetDao: WidgetDao,
    private val albumDao: AlbumDao,
    private val widgetDataStore: DataStore<SerializedWidgetState>,
) : OagRepository {
    override val projectId: Flow<String?> = dataStore.projectId

//    override val widget = projectId.mapNotNull { it }.flatMapLatest {
//        widgetDao.getWidgetFlow(it).map { it?.asExternalModel() }
//    }

//    override suspend fun getWidget(projectId: String): OAGWidget? = withContext(ioDispatcher) {
//        widgetDao.getWidget(projectId)?.asExternalModel()
//    }

    override suspend fun setProject(projectId: String): Project? = withContext(Dispatchers.IO) {
        dataStore.setProjectId(projectId)
        getProject(projectId)
//        updateDailyAlbum(projectId)
//        Unit
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

//    override val project: Flow<Project?> = widgetDataStore.data.map { it }

    override val project: Flow<Project?> = projectId.mapNotNull { it }.flatMapLatest {
        projectDao.getProject(it).map {
            it?.asExternalModel()
        }
    }

    override val albums: Flow<List<Album>> = albumDao.getAlbums().map { albums ->
        albums.map { album ->
            album.asExternalModel()
        }
    }

    override suspend fun getProject(projectId: String): Project? {
        widgetDataStore.updateData { oldState ->
            when (oldState) {
                is Success -> oldState
                else -> Loading(oldState.projectId)
            }
        }

        val projectRes = networkDataSource.getProject(projectId).getOrThrow()

        val project = projectRes?.asExternalModel()

        if (project != null) {
            widgetDataStore.updateData { _ ->
                val latestAlbum = project.history.lastOrNull()
                val newAlbumAvailable = latestAlbum?.rating == Rating.Unrated
                val albumToUse = if (newAlbumAvailable) {
                    latestAlbum?.album ?: project.currentAlbum
                } else project.currentAlbum

                Success(
                    AlbumWidgetData(
                        albumToUse.images.maxBy { it.height }.url, newAlbumAvailable
                    ), projectId
                )
            }
        }

        return project
    }

    override val widgetState = widgetDataStore.data
}
