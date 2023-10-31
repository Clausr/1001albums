package dk.clausr.core.database.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dk.clausr.core.model.Album
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
    fun stringToLocalDateTime(value: String): LocalDateTime {
        return ZonedDateTime.parse(value).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
    }

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'")
        return value.format(formatter)
    }

    @TypeConverter
    fun stringToLocalDate(value: String): LocalDate {
        return LocalDate.parse(value)
    }

    @TypeConverter
    fun localDateToString(value: LocalDate): String {
        return value.toString()
    }
}
