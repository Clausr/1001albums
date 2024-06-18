package dk.clausr.a1001albumsgenerator.network

import dk.clausr.a1001albumsgenerator.network.model.NetworkProject

interface OAGDataSource {
    suspend fun getProject(projectId: String): Result<NetworkProject>
}
