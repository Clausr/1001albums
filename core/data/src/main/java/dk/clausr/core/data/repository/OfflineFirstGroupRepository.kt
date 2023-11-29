package dk.clausr.core.data.repository

import androidx.datastore.core.DataStore
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Loading
import dk.clausr.core.data_widget.SerializedWidgetState.Success
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.WidgetDao
import dk.clausr.core.database.model.asExternalModel
import dk.clausr.core.datastore.OagDataStore
import dk.clausr.core.model.Album
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    override val widgetState = widgetDataStore.data

    override suspend fun setProject(projectId: String): Project? = withContext(Dispatchers.IO) {
        dataStore.setProjectId(projectId)
        getProject(projectId)
    }

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
            updateWidgetData(project)
        }

        return project
    }

    private suspend fun updateWidgetData(project: Project) {
        widgetDataStore.updateData { _ ->
            val latestAlbum = project.history.lastOrNull()
            val newAlbumAvailable = latestAlbum?.rating == Rating.Unrated
            val albumToUse = if (newAlbumAvailable) {
                latestAlbum?.album ?: project.currentAlbum
            } else project.currentAlbum

            Success(
                AlbumWidgetData(
                    albumToUse.images.maxBy { it.height }.url, newAlbumAvailable
                ), project.name
            )
        }
    }

    override suspend fun updateProject(): Project? {
        val projectId = CoroutineScope(Dispatchers.IO).run { projectId.first() } ?: return null

        return getProject(projectId)
    }
}
