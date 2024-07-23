package dk.clausr.core.common.model

import dk.clausr.core.common.BuildConfig

sealed class Result<out T, out E> {
    data class Success<out R>(val value: R) : Result<R, Nothing>()
    data class Failure<out E>(val reason: E, val throwable: Throwable? = defaultCause()) : Result<Nothing, E>()

    fun getOrNull(): T? {
        return when (this) {
            is Success -> value
            is Failure -> null
        }
    }

    companion object {
        internal fun defaultCause(): Throwable? {
            return if (BuildConfig.DEBUG) {
                IllegalStateException("Result.Failure occurred. Maybe the stacktrace can provide some insight.")
            } else {
                null
            }
        }
    }
}

inline fun <T, E> Result<T, E>.doOnSuccess(block: (value: T) -> Unit): Result<T, E> {
    if (this is Result.Success) {
        block(this.value)
    }
    return this
}

inline fun <T, E> Result<T, E>.doOnFailure(block: (E) -> Unit): Result<T, E> {
    if (this is Result.Failure) {
        block(this.reason)
    }
    return this
}

/**
 * Map a function over the [Result.Success.value] of a successful Result.
 */
inline fun <T, Tʹ, E> Result<T, E>.map(transform: (T) -> Tʹ): Result<Tʹ, E> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> this
}

/**
 * Flat-map a function over the [Result.Success.value] of a successful Result.
 */
inline fun <T, Tʹ, E> Result<T, E>.flatMap(f: (T) -> Result<Tʹ, E>): Result<Tʹ, E> = when (this) {
    is Result.Success<T> -> f(value)
    is Result.Failure<E> -> this
}

/**
 * Flat-map a function over the [Result.Failure.reason] of a failed Result.
 */
inline fun <T, E, Eʹ> Result<T, E>.flatMapError(f: (E) -> Result<T, Eʹ>): Result<T, Eʹ> = when (this) {
    is Result.Success<T> -> this
    is Result.Failure<E> -> f(reason)
}

/**
 * Map a function over the [Result.Failure.reason] of a failed Result.
 */
inline fun <T, E, Eʹ> Result<T, E>.mapError(f: (E) -> Eʹ): Result<T, Eʹ> = when (this) {
    is Result.Success<T> -> this
    is Result.Failure<E> -> Result.Failure(f(reason))
}

/**
 * On failure, switch to [transformContent] and resume with [Result.Success]
 */
inline fun <T, E> Result<T, E>.onErrorResume(transformContent: (E) -> T): Result.Success<T> {
    return when (this) {
        is Result.Failure -> Result.Success(transformContent(reason))
        is Result.Success -> this
    }
}
