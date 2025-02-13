package dk.clausr.a1001albumsgenerator.ui.preview

import androidx.compose.ui.graphics.Color

internal fun consistentRandomColor(key: String): Color {
    val hash = key.hashCode() // Generate a hash from the key
    val red = (hash shr 16 and 0xFF).toFloat() / 255
    val green = (hash shr 8 and 0xFF).toFloat() / 255
    val blue = (hash and 0xFF).toFloat() / 255

    return Color(red = red, green = green, blue = blue, alpha = 1f) // Fully opaque color
}