package dk.clausr.a1001albumsgenerator.network

import dk.clausr.a1001albumsgenerator.network.model.NetworkGroup
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject

interface OAGDataSource {
    suspend fun getGroup(groupId: String): NetworkGroup
    suspend fun getProject(projectId: String): NetworkProject
}
