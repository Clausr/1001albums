package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.clausr.a1001albumsgenerator.network.OAGNetworkDataSource
import dk.clausr.a1001albumsgenerator.network.model.NetworkGroup
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.Retrofit
import javax.inject.Inject


class OAGRetrofitDataSource @Inject constructor(
    retrofit: Retrofit,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : OAGNetworkDataSource {
    private val api = retrofit.create(OAGRetrofitApi::class.java)
    override suspend fun getGroup(groupId: String): Result<NetworkGroup?> = try {
        val groupResponse = api.getGroups(groupId)

        if (groupResponse.isSuccessful && groupResponse.body() != null) {
            Result.success(groupResponse.body())
        } else Result.failure(Exception("No result"))
    } catch (e: Exception) {
        Result.failure(e)
    }


    override suspend fun getProject(projectId: String): Result<NetworkProject?> = try {
        val projectResponse = api.getProject(projectId)

        if (projectResponse.isSuccessful && projectResponse.body() != null) {
            Result.success(projectResponse.body())
        } else Result.failure(Exception("No project response"))
    } catch (e: Exception) {
        Result.failure(e)
    }

}
