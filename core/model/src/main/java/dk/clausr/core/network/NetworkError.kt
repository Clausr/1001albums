package dk.clausr.core.network

sealed class NetworkError(val cause: Throwable? = null) {
    class TooManyRequests(cause: Throwable? = null) : NetworkError(cause)
    class ProjectNotFound(cause: Throwable? = null) : NetworkError(cause)
    class Generic(cause: Throwable? = null) : NetworkError(cause)
    data object NoGroup : NetworkError()
}
