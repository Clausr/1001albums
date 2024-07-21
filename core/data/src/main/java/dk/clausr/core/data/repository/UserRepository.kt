package dk.clausr.core.data.repository

import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.user.UserState
import dk.clausr.core.model.StreamingPlatform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    oagRepository: OagRepository,
) {
    val userState: Flow<UserState> = oagRepository.project.map {
        if (it == null) {
            UserState.NotOnboarded
        } else {
            UserState.Active(projectId = it.name, preferredStreamingPlatform = StreamingPlatform.Tidal)
        }
    }
//    val projectId: Flow<String?>
}