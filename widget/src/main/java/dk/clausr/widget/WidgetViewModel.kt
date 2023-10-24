package dk.clausr.widget

import androidx.annotation.WorkerThread
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Group
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    @WorkerThread
    fun getGroup(groupId: String = "claus-rasmus-delemusik"): Flow<Group> {
        Timber.d("Get group: $groupId")
        return oagRepository.getGroup(groupId).flowOn(ioDispatcher)
    }

}
