package dk.clausr.core.data.repository

import androidx.datastore.core.DataStore
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Loading
import dk.clausr.core.data_widget.SerializedWidgetState.Success
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OfflineFirstOagRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val widgetDataStore: DataStore<SerializedWidgetState>,
) : OagRepository {
    override val widgetState = widgetDataStore.data.map {
        Timber.d("WidgetState changed: $it")
        it
    }

    override val projectId: Flow<String?> = widgetDataStore.data.map {
        Timber.d("ProjectId: $it")
        it.projectId
    }

    override val project: Flow<Project?> = projectId
        .mapNotNull { it }
        .distinctUntilChanged()
        .mapLatest {
            Timber.d("project flow : $it")
            getAndUpdateProject(it)
        }

    override suspend fun setProject(projectId: String) = withContext(ioDispatcher) {
        Timber.d("OfflineFirstRepo - setProject $projectId")
//        widgetDataStore.updateData { Loading(projectId) }
        getAndUpdateProject(projectId)
        Unit
    }

    private suspend fun getAndUpdateProject(projectId: String): Project? {
        Timber.d("getAndUpdateProject")
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
        Timber.d("Update widget data")
        widgetDataStore.updateData { _ ->
            val latestAlbum = project.history.firstOrNull { it.rating == Rating.Unrated }
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

    override suspend fun updateProject() {
        Timber.d("Update project")
        val projectId = CoroutineScope(Dispatchers.IO).run { projectId.first() } ?: return

        getAndUpdateProject(projectId)
    }
}
