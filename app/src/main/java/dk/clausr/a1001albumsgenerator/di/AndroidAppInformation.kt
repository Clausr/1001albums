package dk.clausr.a1001albumsgenerator.di

import dk.clausr.a1001albumsgenerator.BuildConfig
import dk.clausr.core.common.network.AppInformation

class AndroidAppInformation : AppInformation {
    override val versionCode: String = BuildConfig.VERSION_CODE.toString()
    override val versionName: String = BuildConfig.VERSION_NAME
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val userAgent: String
        get() = "OAG-App/Android/$applicationId/$versionName-$versionCode"
}
