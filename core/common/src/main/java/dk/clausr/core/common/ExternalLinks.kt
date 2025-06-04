package dk.clausr.core.common

import android.net.Uri

object ExternalLinks {
    object Clausr {
        private const val BASE_URL = "https://www.clausr.dk"
        const val PRIVACY_POLICY = "$BASE_URL/privacy"
    }

    object Generator {
        const val BASE_URL = "https://1001albumsgenerator.com"

        fun historyLink(
            projectId: String,
            albumId: String,
        ): String = Uri.Builder()
            .scheme("https")
            .authority("1001albumsgenerator.com")
            .path("$projectId/history")
            .fragment("project-history--rating-col-$albumId")
            .build()
            .toString()
    }
}
