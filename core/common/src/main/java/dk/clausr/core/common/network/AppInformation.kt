package dk.clausr.core.common.network

interface AppInformation {
    val userAgent: String
    val versionCode: String
    val versionName: String
    val applicationId: String
}
