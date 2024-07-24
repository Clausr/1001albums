package dk.clausr.a1001albumsgenerator.utils

import dk.clausr.core.common.model.Result
import dk.clausr.core.network.NetworkError
import retrofit2.HttpException

internal inline fun <T> doNetwork(
    httpExceptionHandler: (e: HttpException) -> Result.Failure<NetworkError> = ::defaultHttpExceptionHandler,
    block: () -> T,
): Result<T, NetworkError> {
    return try {
        val value = block()

        Result.Success(value)
    } catch (httpException: HttpException) {
        httpExceptionHandler(httpException)
    } catch (e: Exception) {
        Result.Failure(NetworkError.Generic(e))
    }
}

fun defaultHttpExceptionHandler(e: HttpException): Result.Failure<NetworkError> {
    val code = e.code()
    val message = e.message()

    return Result.Failure(
        when (code) {
            HttpStatusCodes.NOT_FOUND -> NetworkError.ProjectNotFound(e)
            HttpStatusCodes.TOO_MANY_REQUESTS -> NetworkError.TooManyRequests(e)
            else -> NetworkError.Generic(e)
        },
    )
}
