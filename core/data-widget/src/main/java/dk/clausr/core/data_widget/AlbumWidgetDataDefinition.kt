package dk.clausr.core.data_widget

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

object AlbumWidgetDataDefinition : GlanceStateDefinition<SerializedWidgetState> {
    const val fileName = "_ALBUM_WIDGET_DATASTORE_FILE"
    private val Context.oagDataStore by dataStore(fileName, WidgetStateDataSerializer)
    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<SerializedWidgetState> = context.oagDataStore

    suspend fun getDataStore(context: Context): DataStore<SerializedWidgetState> =
        getDataStore(context, fileName)

    override fun getLocation(context: Context, fileKey: String): File =
        context.dataStoreFile(fileName)

    @OptIn(ExperimentalSerializationApi::class)
    object WidgetStateDataSerializer : Serializer<SerializedWidgetState> {

        private val kSerializable = SerializedWidgetState.serializer()

        override val defaultValue: SerializedWidgetState
            get() = SerializedWidgetState.NotInitialized

        private val json = Json {
            serializersModule = SerializersModule {
                contextual(Instant::class, InstantSerializer)
            }
        }

        override suspend fun readFrom(input: InputStream): SerializedWidgetState {
            return try {
                input.use { stream ->
                    json.decodeFromStream(kSerializable, stream)
                }
            } catch (exception: SerializationException) {
                throw CorruptionException("Could not read location data: ${exception.message}")
            } catch (e: Exception) {
                throw e
            }
        }

        override suspend fun writeTo(t: SerializedWidgetState, output: OutputStream) {
            output.use { stream ->
                json.encodeToStream(kSerializable, t, stream)
            }
        }
    }
}
