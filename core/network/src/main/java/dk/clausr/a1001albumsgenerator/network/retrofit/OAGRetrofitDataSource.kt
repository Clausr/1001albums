package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import retrofit2.Retrofit
import javax.inject.Inject


class OAGRetrofitDataSource @Inject constructor(
    retrofit: Retrofit,
) : OAGDataSource {
    private val api = retrofit.create(OAGRetrofitApi::class.java)

    override suspend fun getProject(projectId: String): Result<NetworkProject?> = try {
        val projectResponse = api.getProject(projectId)

        if (projectResponse.isSuccessful && projectResponse.body() != null) {
            Result.success(projectResponse.body())
        } else Result.failure(Exception("No project response"))
    } catch (e: Exception) {
        Result.failure(e)
    }

}
