package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.asExternalModel
import dk.clausr.core.model.Group
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class OfflineFirstGroupRepository @Inject constructor(
    private val dataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : OagRepository {

    override fun getGroup(groupId: String): Flow<Group> = flow {
        Timber.d("Get group repo")
        emit(dataSource.getGroup(groupId).asExternalModel())
    }.flowOn(ioDispatcher)
}
