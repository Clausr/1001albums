package dk.clausr.core.database.utils

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long): Instant? = Instant.ofEpochMilli(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = Json.encodeToString(stringListSerializer, value)

    @TypeConverter
    fun toStringList(value: String): List<String> = Json.decodeFromString(stringListSerializer, value)

    private companion object {
        val stringListSerializer = ListSerializer(String.serializer())
    }
}
