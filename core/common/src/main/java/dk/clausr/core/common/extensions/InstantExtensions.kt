package dk.clausr.core.common.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Instant.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime {
    return this.atZone(zoneId).toLocalDateTime()
}

fun LocalDateTime.formatToLocalDate(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
    return this.format(formatter)
}

fun Instant.formatToDate(): String {
    val formattedDate = this.toLocalDateTime()
    return formattedDate.formatToLocalDate()
}