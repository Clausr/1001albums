package dk.clausr.feature.overview

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import dk.clausr.a1001albumsgenerator.ui.navigation.sharedTransitionComposable
import dk.clausr.core.common.android.navArg
import dk.clausr.feature.overview.details.AlbumDetailsRoute

object OverviewDirections {
    object Routes {
        internal const val OVERVIEW_ROOT = "OverviewRoot"
        internal const val OVERVIEW = "$OVERVIEW_ROOT/Overview"
        internal const val ALBUM_DETAILS = "$OVERVIEW_ROOT/AlbumDetails?${Args.ALBUM_SLUG}?{${Args.ALBUM_SLUG}}"
    }

    object Args {
        const val ALBUM_SLUG = "albumSlug"
    }

    fun root() = Routes.OVERVIEW_ROOT
    fun overview() = Routes.OVERVIEW
    fun albumDetails(slug: String) = Routes.ALBUM_DETAILS.navArg(Args.ALBUM_SLUG, slug)
}

fun NavGraphBuilder.overviewGraph(
    navHostController: NavHostController,
    navigateToSettings: () -> Unit,
) {
    navigation(
        route = OverviewDirections.root(),
        startDestination = OverviewDirections.overview(),
    ) {
        sharedTransitionComposable(route = OverviewDirections.Routes.OVERVIEW) {
            OverviewRoute(
                navigateToSettings = navigateToSettings,
                navigateToAlbumDetails = { slug ->
                    navHostController.navigate(OverviewDirections.albumDetails(slug))
                },
            )
        }

        sharedTransitionComposable(
            route = OverviewDirections.Routes.ALBUM_DETAILS,
            arguments = listOf(
                navArgument(OverviewDirections.Args.ALBUM_SLUG) { type = NavType.StringType },
            )
        ) {
            AlbumDetailsRoute()
        }
    }
}