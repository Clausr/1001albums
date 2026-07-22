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
            groupId: String?,
        ): String = Uri.Builder()
            .scheme("https")
            .authority("1001albumsgenerator.com")
            .path("$projectId/history")
            // The row id suffix is the group the album was generated in (or "false" when solo).
            // ponytail: uses the current group id for every row — correct for single-group users;
            // per-album group tagging would need the API to expose it in the project history.
            .fragment("project-history--album-row-$albumId-${groupId ?: "false"}")
            .build()
            .toString()
    }
}
