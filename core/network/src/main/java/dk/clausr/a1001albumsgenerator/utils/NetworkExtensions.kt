package dk.clausr.a1001albumsgenerator.utils

import dk.clausr.core.common.model.Result

internal inline fun <T> doNetwork(
    block: () -> T
): Result<T> {
    return try {
        val value = block()

        Result.Success(value)
    } catch (e: Exception) {
        Result.Failure(e.message, e.cause)
    }
}