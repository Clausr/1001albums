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
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object AlbumWidgetDataDefinition : GlanceStateDefinition<SerializedWidgetState> {
    private const val FILE_NAME = "_ALBUM_WIDGET_DATASTORE_FILE"
    private val Context.oagDataStore by dataStore(
        fileName = FILE_NAME,
        serializer = WidgetStateDataSerializer,
        produceMigrations = { _ ->
            listOf(dataMigration)
        },
    )

    override suspend fun getDataStore(
        context: Context,
        fileKey: String,
    ): DataStore<SerializedWidgetState> = context.oagDataStore

    suspend fun getDataStore(context: Context): DataStore<SerializedWidgetState> = getDataStore(context, FILE_NAME)

    override fun getLocation(
        context: Context,
        fileKey: String,
    ): File = context.dataStoreFile(FILE_NAME)

    @OptIn(ExperimentalSerializationApi::class)
    object WidgetStateDataSerializer : Serializer<SerializedWidgetState> {

        private val kSerializable = SerializedWidgetState.serializer()

        override val defaultValue: SerializedWidgetState
            get() = SerializedWidgetState.NotInitialized

        private val json = Json

        override suspend fun readFrom(input: InputStream): SerializedWidgetState {
            return try {
                input.use { stream ->
                    json.decodeFromStream(kSerializable, stream)
                }
            } catch (exception: SerializationException) {
                throw CorruptionException("Could not read location data: ${exception.message}")
            } catch (e: Exception) {
                Timber.e(e, "Exception")
                throw e
            }
        }

        override suspend fun writeTo(
            t: SerializedWidgetState,
            output: OutputStream,
        ) {
            output.use { stream ->
                json.encodeToStream(kSerializable, t, stream)
            }
        }
    }
}
