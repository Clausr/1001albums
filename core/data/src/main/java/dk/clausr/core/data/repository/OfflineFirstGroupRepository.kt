package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.OAGNetworkDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.data.model.toEntity
import dk.clausr.core.database.dao.ProjectDao
import dk.clausr.core.database.dao.WidgetDao
import dk.clausr.core.database.model.WidgetEntity
import dk.clausr.core.database.model.asExternalModel
import dk.clausr.core.datastore.OagDataStore
import dk.clausr.core.model.Group
import dk.clausr.core.model.OAGWidget
import dk.clausr.core.model.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OfflineFirstGroupRepository @Inject constructor(
    private val networkDataSource: OAGNetworkDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val dataStore: OagDataStore,
    private val projectDao: ProjectDao,
    private val widgetDao: WidgetDao,
) : OagRepository {
    override val projectId: Flow<String?> = dataStore.projectId
    override val groupId: Flow<String?> = dataStore.groupId

    override fun getWidgetFlow(projectId: String): Flow<OAGWidget?> {
        return widgetDao.getWidgetFlow(projectId).map { it?.asExternalModel() }
    }

    override suspend fun getWidget(projectId: String): OAGWidget? = withContext(ioDispatcher) {
        widgetDao.getWidget(projectId)?.asExternalModel()
    }

    override fun getGroup(groupId: String): Flow<Group?> = flow {
        dataStore.setGroup(groupId)

        networkDataSource.getGroup(groupId).apply {
            onSuccess {
                emit(it?.asExternalModel())
            }
            onFailure {
                Timber.w("Could not get group..")
                emit(null)
            }
        }

    }.flowOn(ioDispatcher)

    override suspend fun setProject(projectId: String) {
        dataStore.setProject(projectId)
        val projectRes = networkDataSource.getProject(projectId).getOrThrow()
        projectRes?.toEntity()?.let { projectDao.insertProject(it) }
        projectRes?.currentAlbum?.let {
            val widget = WidgetEntity(projectId, it.name, it.artist, it.images.maxBy { it.height }.url)
            widgetDao.insert(widget)
        }
//        projectRes?.currentAlbum?.toEntity()?.let { albumDao.insert(it) }

    }

    override suspend fun updateDailyAlbum(projectId: String) {
        val projectRes = networkDataSource.getProject(projectId).getOrThrow()
        projectRes?.currentAlbum?.let {
            val widget = WidgetEntity(projectId, it.name, it.artist, it.images.maxBy { it.height }.url)
            widgetDao.insert(widget)
        }
    }

    override fun getProjectFlow(projectId: String): Flow<Project?> = projectDao.getProject(projectId).map { it?.asExternalModel() }
//    flow {
//        dataStore.setProject(projectId)
//        dataSource.getProject(projectId).apply {
//            onSuccess {
//                emit(it?.asExternalModel())
//            }
//            onFailure {
//                Timber.w("Could not get project..")
//                emit(null)
//            }
//        }
//    }.flowOn(ioDispatcher)

    override suspend fun getProject(projectId: String): Project? {
        val projectRes = networkDataSource.getProject(projectId).getOrThrow()

        return projectRes?.asExternalModel()
    }
}
