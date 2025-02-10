package dk.clausr.feature.overview.navigation

import dk.clausr.core.common.android.navArg

object OverviewDirections {
    object Routes {
        internal const val OVERVIEW_ROOT = "OverviewRoot"
        internal const val OVERVIEW = "$OVERVIEW_ROOT/Overview"
        internal const val ALBUM_DETAILS = "$OVERVIEW_ROOT/AlbumDetails/{${Args.ALBUM_ID}}?${Args.LIST_NAME}={${Args.LIST_NAME}}"
    }

    object Args {
        const val ALBUM_ID = "albumId"
        const val LIST_NAME = "listName"
    }

    fun root() = Routes.OVERVIEW_ROOT
    fun overview() = Routes.OVERVIEW
    fun albumDetails(
        id: String,
        listName: String,
    ) = Routes.ALBUM_DETAILS.navArg(Args.ALBUM_ID, id).navArg(Args.LIST_NAME, listName)
}
