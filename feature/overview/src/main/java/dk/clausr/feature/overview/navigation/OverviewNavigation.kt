package dk.clausr.feature.overview.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import dk.clausr.a1001albumsgenerator.ui.navigation.sharedTransitionComposable
import dk.clausr.feature.overview.OverviewRoute
import dk.clausr.feature.overview.details.AlbumDetailsRoute

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
                navigateToAlbumDetails = { id, listName ->
                    navHostController.navigate(
                        OverviewDirections.albumDetails(
                            id = id,
                            listName = listName,
                        ),
                    )
                },
            )
        }

        sharedTransitionComposable(
            route = OverviewDirections.Routes.ALBUM_DETAILS,
            arguments = listOf(
                navArgument(OverviewDirections.Args.ALBUM_ID) { type = NavType.StringType },
                navArgument(OverviewDirections.Args.LIST_NAME) { type = NavType.StringType },
            ),
        ) {
            AlbumDetailsRoute(
                onNavigateBack = {
                    navHostController.navigateUp()
                },
                navigateToDetails = { id, list ->
                    navHostController.navigate(
                        OverviewDirections.albumDetails(
                            id = id,
                            listName = list,
                        ),
                    ) {
                        popUpTo(OverviewDirections.Routes.ALBUM_DETAILS) { inclusive = true }
                    }
                },
            )
        }
    }
}
