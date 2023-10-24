package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.model.NetworkGroupResponse
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import javax.inject.Inject


class OAGNetworkDataSource @Inject constructor(
    retrofit: Retrofit,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : OAGDataSource {
    private val api = retrofit.create(OAGRetrofitApi::class.java)
    override suspend fun getGroup(groupId: String): NetworkGroupResponse = withContext(ioDispatcher) {
        api.getGroups(groupId)
    }

}
