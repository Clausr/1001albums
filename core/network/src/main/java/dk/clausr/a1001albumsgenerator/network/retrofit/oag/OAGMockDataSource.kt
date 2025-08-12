package dk.clausr.a1001albumsgenerator.network.retrofit.oag

import dk.LocalAssetManager
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.a1001albumsgenerator.network.fake.FakeAssetManager
import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbumGroupReviews
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.common.model.Result
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.network.NetworkError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

@OptIn(ExperimentalSerializationApi::class)
class OAGMockDataSource @Inject constructor(
    private val json: Json,
    private val assets: FakeAssetManager = LocalAssetManager,
    @param:Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : OAGDataSource {
    override suspend fun getProject(projectId: String): Result<NetworkProject, NetworkError> = withContext(ioDispatcher) {
        Result.Success(assets.open("mock_project_response.json").use(json::decodeFromStream))
    }

    override suspend fun getGroupReviewsForAlbum(
        groupSlug: String,
        albumId: String,
    ): Result<NetworkAlbumGroupReviews, NetworkError> = withContext(ioDispatcher) {
        Result.Success(assets.open("mock_album_group_review.json").use(json::decodeFromStream))
    }
}
