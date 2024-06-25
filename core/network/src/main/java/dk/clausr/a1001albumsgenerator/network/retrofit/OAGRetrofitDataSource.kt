package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.a1001albumsgenerator.utils.doNetwork
import dk.clausr.core.common.model.Result
import retrofit2.Retrofit
import javax.inject.Inject

class OAGRetrofitDataSource @Inject constructor(
    retrofit: Retrofit,
) : OAGDataSource {
    private val api = retrofit.create(OAGRetrofitApi::class.java)

    override suspend fun getProject(projectId: String): Result<NetworkProject> = doNetwork {
        api.getProject(projectId)
    }
}
