package dk.clausr.a1001albumsgenerator.network

import dk.clausr.a1001albumsgenerator.network.model.NetworkGroupResponse

interface OAGDataSource {
    suspend fun getGroup(groupId: String): NetworkGroupResponse
}
