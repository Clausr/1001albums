package dk.clausr.core.common

object ExternalLinks {
    object Clausr {
        private const val BASE_URL = "https://www.clausr.dk"
        const val PRIVACY_POLICY = "$BASE_URL/privacy"
    }

    object Generator {
        const val BASE_URL = "https://1001albumsgenerator.com"

        fun groupRatingDetails(
            groupName: String,
            albumId: String,
        ): String = "$BASE_URL/group/$groupName/album/$albumId"

    }
}
