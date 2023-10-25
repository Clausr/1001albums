package dk.clausr.widget

import androidx.annotation.WorkerThread
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Group
import dk.clausr.core.model.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    val viewModelScope = CoroutineScope(ioDispatcher + SupervisorJob())
    private val refresh: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)
    val project: Flow<Project?> = combine(oagRepository.projectId, refresh) { projectId, _ ->
        projectId
    }
        .distinctUntilChanged()
        .map {
            if (it?.isNotBlank() == true) {
                oagRepository.getProject(it).first()
            } else null
        }

    init {
        viewModelScope.launch {
            refresh.emit(Unit)
        }
    }

    @WorkerThread
    fun getGroup(groupId: String = "claus-rasmus-delemusik"): Flow<Group> {
        Timber.d("Get group: $groupId")
        return oagRepository.getGroup(groupId).flowOn(ioDispatcher)
    }

    fun refresh() = CoroutineScope(ioDispatcher).launch {
        refresh.emit(Unit)
    }

}
