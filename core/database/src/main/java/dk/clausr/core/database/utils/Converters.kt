package dk.clausr.core.database.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dk.clausr.core.model.Album
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromAlbumImages(images: List<Album.AlbumImage>): String {
        return Gson().toJson(images)
    }

    @TypeConverter
    fun toAlbumImages(imagesString: String): List<Album.AlbumImage> {
        val listType = object : TypeToken<List<Album.AlbumImage>>() {}.type
        return Gson().fromJson(imagesString, listType)
    }

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long): Instant? = Instant.ofEpochMilli(value)
}
