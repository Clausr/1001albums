package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.datastore.OagDataStore
import dk.clausr.core.model.Group
import dk.clausr.core.model.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class OfflineFirstGroupRepository @Inject constructor(
    private val dataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val dataStore: OagDataStore,
) : OagRepository {
    override val projectId: Flow<String?> = dataStore.projectId
    override val groupId: Flow<String?> = dataStore.groupId

    override fun getGroup(groupId: String): Flow<Group?> = flow {
        dataStore.setGroup(groupId)

        dataSource.getGroup(groupId).apply {
            onSuccess {
                emit(it?.asExternalModel())
            }
            onFailure {
                Timber.w("Could not get group..")
                emit(null)
            }
        }

    }.flowOn(ioDispatcher)

    override fun getProject(projectId: String): Flow<Project?> = flow {
        dataStore.setProject(projectId)
        dataSource.getProject(projectId).apply {
            onSuccess {
                emit(it?.asExternalModel())
            }
            onFailure {
                Timber.w("Could not get project..")
                emit(null)
            }
        }
    }.flowOn(ioDispatcher)
}
