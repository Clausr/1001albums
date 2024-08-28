package dk.clausr.feature.overview.navigation

import dk.clausr.core.common.android.navArg

object OverviewDirections {
    object Routes {
        internal const val OVERVIEW_ROOT = "OverviewRoot"
        internal const val OVERVIEW = "$OVERVIEW_ROOT/Overview"
        internal const val ALBUM_DETAILS = "$OVERVIEW_ROOT/AlbumDetails/{${Args.ALBUM_SLUG}}?${Args.LIST_NAME}={${Args.LIST_NAME}}"
    }

    object Args {
        const val ALBUM_SLUG = "albumSlug"
        const val LIST_NAME = "listName"
    }

    fun root() = Routes.OVERVIEW_ROOT
    fun overview() = Routes.OVERVIEW
    fun albumDetails(
        slug: String,
        listName: String,
    ) = Routes.ALBUM_DETAILS.navArg(Args.ALBUM_SLUG, slug).navArg(Args.LIST_NAME, listName)
}
