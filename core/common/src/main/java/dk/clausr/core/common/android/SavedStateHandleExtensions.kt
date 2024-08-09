package dk.clausr.core.common.android

import androidx.lifecycle.SavedStateHandle
import kotlin.properties.ReadOnlyProperty

inline fun <reified T> SavedStateHandle.require(key: String? = null): ReadOnlyProperty<Any, T> = ReadOnlyProperty { _, property ->
    val stateKey = key ?: property.name
    requireNotNull(this@require[stateKey])
}