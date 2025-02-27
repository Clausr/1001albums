package dk.clausr.core.common.extensions

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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

fun LocalDate.formatMonthAndYear(locale: Locale = Locale.getDefault()): String {
    val currentYear = LocalDate.now().year
    val formatter = if (this.year == currentYear) {
        DateTimeFormatter.ofPattern("MMMM", locale) // Just the month
    } else {
        DateTimeFormatter.ofPattern("MMMM yyyy", locale) // Month and year
    }
    return this.format(formatter)
}

fun Instant.toLocalizedDateTime(): String = toLocalDateTime()
    .format(
        DateTimeFormatter.ofLocalizedDateTime(
            /*dateStyle = */ FormatStyle.LONG,
            /*timeStyle*/ FormatStyle.SHORT,
        ),
    )
