package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.LocalAssetManager
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.fake.FakeAssetManager
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

@OptIn(ExperimentalSerializationApi::class)
class DemoDataSource @Inject constructor(
    private val json: Json,
    private val assets: FakeAssetManager = LocalAssetManager,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : OAGDataSource {
    override suspend fun getProject(projectId: String): Result<NetworkProject?> = withContext(ioDispatcher) {
        Result.success(assets.open("mock_project_response.json").use(json::decodeFromStream))
    }
}
