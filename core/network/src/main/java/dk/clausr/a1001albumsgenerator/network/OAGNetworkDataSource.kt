package dk.clausr.a1001albumsgenerator.network

import dk.clausr.a1001albumsgenerator.network.model.NetworkGroup
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject

interface OAGNetworkDataSource {
    suspend fun getGroup(groupId: String): Result<NetworkGroup?>
    suspend fun getProject(projectId: String): Result<NetworkProject?>
}
