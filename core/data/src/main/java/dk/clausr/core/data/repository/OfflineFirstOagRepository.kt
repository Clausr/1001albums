package dk.clausr.core.data.repository

import androidx.datastore.core.DataStore
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data.model.toEntity
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectId
import dk.clausr.core.database.dao.AlbumDao
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingLink
import dk.clausr.core.model.StreamingLinks
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
import javax.inject.Singleton

@Singleton
class OfflineFirstOagRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val widgetDataStore: DataStore<SerializedWidgetState>,
    private val albumDao: AlbumDao,
    private val projectDao: ProjectDao,
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
        getAndUpdateProject(projectId)
        Unit
    }

    private suspend fun getAndUpdateProject(projectId: String): Project? =
        withContext(ioDispatcher) {
            Timber.d("getAndUpdateProject")

            val proj = networkDataSource.getProject(projectId)
                .onSuccess { networkProject ->
                    Timber.d("Got project ${networkProject.name} -- ${networkProject.history.size} albums!")
                    projectDao.insertProject(networkProject.toEntity())
                    // TODO Maybe not..
                    albumDao.clearTable()
                    albumDao.insertAlbums(networkProject.history.map { it.album.toEntity() })
                    networkProject.asExternalModel()
                }
                .onFailure {
                    Timber.e(it, "Project failure")
                    null
                }

            val project = proj.getOrNull()?.asExternalModel()?.apply {
                updateWidgetData(this)
            }

            project
        }

    private suspend fun updateWidgetData(project: Project) {
        Timber.d("Update widget data")
        widgetDataStore.updateData { _ ->
            val latestAlbum = project.history.firstOrNull { it.rating == Rating.Unrated }
            val newAlbumAvailable = latestAlbum?.rating == Rating.Unrated
            val albumToUse = if (newAlbumAvailable) {
                latestAlbum?.album ?: project.currentAlbum
            } else project.currentAlbum

            SerializedWidgetState.Success(
                data = AlbumWidgetData(
                    coverUrl = albumToUse.images.maxBy { it.height }.url,
                    newAvailable = newAlbumAvailable,
                    wikiLink = albumToUse.wikipediaUrl,
                    streamingLinks = StreamingLinks(
                        listOfNotNull(
                            albumToUse.tidalId?.let {
                                StreamingLink(
                                    link = "https://tidal.com/browse/album/$it",
                                    name = "Tidal",
                                )
                            },
                        )
                    ),
                ),
                currentProjectId = project.name
            )
        }
    }

    override suspend fun updateProject() {
        Timber.d("Update project")
        val projectId = CoroutineScope(Dispatchers.IO).run { projectId.first() } ?: return

        getAndUpdateProject(projectId)
    }
}
