package dk.clausr.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class StreamingServiceTest {
    @Test
    fun `qobuz links use the Android app link host and preserve the album ID`() {
        for (id in listOf("0884977412833", "mqo28qafeaibb")) {
            val service = StreamingService(id = id, platform = StreamingPlatform.Qobuz)

            assertEquals("https://open.qobuz.com/album/$id", service.streamingLink)
            assertEquals(
                service.streamingLink,
                StreamingServices(listOf(service)).getStreamingLinkFor(StreamingPlatform.Qobuz),
            )
        }
    }
}
