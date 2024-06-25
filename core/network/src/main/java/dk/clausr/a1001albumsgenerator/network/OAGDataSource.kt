package dk.clausr.a1001albumsgenerator.network

import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.common.model.Result

interface OAGDataSource {
    suspend fun getProject(projectId: String): Result<NetworkProject>
}
