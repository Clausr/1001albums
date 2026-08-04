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
            // Row id suffix is the id of the group the album was generated in; solo albums render
            // an empty suffix on the site (confirmed: id="...-<albumId>-"), so null -> "".
            // ponytail: uses the current group id for every row — correct for single-group users;
            // per-album group tagging would need the API to expose it in the project history.
            .fragment("project-history--album-row-$albumId-${groupId.orEmpty()}")
            .build()
            .toString()
    }
}
