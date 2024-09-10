package dk.clausr.a1001albumsgenerator.network.interceptors

import dk.clausr.core.common.network.AppInformation
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class UserAgentInterceptor @Inject constructor(
    private val appInformation: AppInformation,
) : Interceptor {

    companion object {
        // User-Agent
        private const val HEADER_USER_AGENT = "User-Agent"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        requestBuilder.header(HEADER_USER_AGENT, appInformation.userAgent)

        return chain.proceed(requestBuilder.build())
    }
}
