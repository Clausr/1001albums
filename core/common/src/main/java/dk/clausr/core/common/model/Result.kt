package dk.clausr.core.common.model

import dk.clausr.core.common.BuildConfig

sealed class Result<out T> {
    data class Success<out R>(val value: R) : Result<R>()
    data class Failure(val message: String?, val throwable: Throwable? = defaultCause()) :
        Result<Nothing>()

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

inline fun <reified T> Result<T>.doOnSuccess(block: (value: T) -> Unit): Result<T> {
    if (this is Result.Success) {
        block(this.value)
    }
    return this
}

inline fun <E> Result<E>.doOnFailure(block: (message: String?, throwable: Throwable?) -> Unit): Result<E> {
    if (this is Result.Failure) {
        block(message, throwable)
    }
    return this
}

/**
 * Map a function over the [Result.Success.value] of a successful Result.
 */
inline fun <reified T, reified R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> this
}

/**
 * Flat-map a function over the [Result.Success.value] of a successful Result.
 */
inline fun <T, Tʹ> Result<T>.flatMap(f: (T) -> Result<Tʹ>): Result<Tʹ> = when (this) {
    is Result.Success<T> -> f(value)
    is Result.Failure -> this
}


//
///**
// * Flat-map a function over the [Result.Success.value] of a successful Result.
// */
//inline fun <T, Tʹ, E> Result<T, E>.flatMap(f: (T) -> Result<Tʹ, E>): Result<Tʹ, E> = when (this) {
//    is Result.Success<T> -> f(value)
//    is Result.Failure<E> -> this
//}
//
///**
// * Flat-map a function over the [Result.Failure.reason] of a failed Result.
// */
//inline fun <T, E, Eʹ> Result<T, E>.flatMapError(f: (E) -> Result<T, Eʹ>): Result<T, Eʹ> = when (this) {
//    is Result.Success<T> -> this
//    is Result.Failure<E> -> f(reason)
//}
//
///**
// * Map a function over the [Result.Failure.reason] of a failed Result.
// */
//inline fun <T, E, Eʹ> Result<T, E>.mapError(f: (E) -> Eʹ): Result<T, Eʹ> = when (this) {
//    is Result.Success<T> -> this
//    is Result.Failure<E> -> Result.Failure(f(reason))
//}
//
///**
// * On failure, switch to [transformContent] and resume with [Result.Success]
// */
//inline fun <T, E> Result<T, E>.onErrorResume(transformContent: (E) -> T): Result.Success<T> {
//    return when (this) {
//        is Result.Failure -> Result.Success(transformContent(reason))
//        is Result.Success -> this
//    }
//}
