package dk.clausr.feature.overview.navigation

import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import androidx.navigation.navigation
import dk.clausr.a1001albumsgenerator.ui.navigation.sharedTransitionComposable
import dk.clausr.feature.overview.OverviewRoute
import dk.clausr.feature.overview.details.AlbumDetailsScreen
import kotlinx.serialization.Serializable

@Serializable
data object OverviewBaseRoute

@Serializable
data object OverviewRoute

@Serializable
data class AlbumDetailsRoute(val albumId: String, val listName: String)

fun NavController.navigateToAlbumDetails(
    albumId: String,
    listName: String,
    navOptions: NavOptions = navOptions { },
) = navigate(route = AlbumDetailsRoute(albumId, listName), navOptions)

fun NavGraphBuilder.overviewGraph(
    navigateBack: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToAlbumDetails: (albumId: String, list: String) -> Unit,
) {
    navigation<OverviewBaseRoute>(startDestination = OverviewRoute) {
        sharedTransitionComposable<OverviewRoute> {
            OverviewRoute(
                navigateToSettings = navigateToSettings,
                navigateToAlbumDetails = navigateToAlbumDetails,
            )
        }
        sharedTransitionComposable<AlbumDetailsRoute> {
            val uriHandler = LocalUriHandler.current

            AlbumDetailsScreen(
                navigateToDetails = { id, list ->
                    navigateToAlbumDetails(id, list)
                },
                onNavigateBack = navigateBack,
                openLink = { uriHandler.openUri(it) },
            )
        }
    }
}
