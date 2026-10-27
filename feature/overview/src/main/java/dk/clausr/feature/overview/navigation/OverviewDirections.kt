package dk.clausr.feature.overview.navigation

import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import androidx.navigation.navigation
import dk.clausr.a1001albumsgenerator.ui.navigation.sharedTransitionComposable
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.feature.overview.OverviewRoute
import dk.clausr.feature.overview.details.AlbumDetailsScreen
import dk.clausr.feature.overview.details.AlbumKey
import dk.clausr.feature.overview.search.AlbumSearchRoute
import kotlinx.serialization.Serializable

@Serializable
data object OverviewBaseRoute

@Serializable
data object OverviewRoute

@Serializable
data object AlbumSearchRoute

/** The section an album was opened from; the details screen lets the user swipe between its albums. */
@Serializable
enum class AlbumListSource {
    History,
    DidNotListen,
    TopRated,
    Related,

    /** Opened outside a list, e.g. from a notification. Nothing to swipe to. */
    None,
}

/**
 * @param listName Prefix of the shared element keys, so the cover animates back into the thumbnail it came from.
 * @param generatedAt Epoch millis; tells apart entries of the same album that has been generated more than once.
 * @param userAlbums Whether the list is the user albums tab or the 1001 albums tab.
 * @param relatedToAlbumId For [AlbumListSource.Related]: the album whose related row was tapped, which isn't part of the row.
 */
@Serializable
data class AlbumDetailsRoute(
    val albumId: String,
    val listName: String,
    val generatedAt: Long? = null,
    val source: AlbumListSource = AlbumListSource.None,
    val userAlbums: Boolean = false,
    val relatedToAlbumId: String? = null,
    val relatedToGeneratedAt: Long? = null,
)

fun HistoricAlbum.detailsRoute(
    listName: String,
    source: AlbumListSource,
    relatedTo: AlbumKey? = null,
) = AlbumDetailsRoute(
    albumId = album.id,
    listName = listName,
    generatedAt = metadata?.generatedAt?.toEpochMilli(),
    source = source,
    userAlbums = album.isUserAlbum,
    relatedToAlbumId = relatedTo?.id,
    relatedToGeneratedAt = relatedTo?.generatedAt,
)

fun NavController.navigateToAlbumDetails(
    route: AlbumDetailsRoute,
    navOptions: NavOptions = navOptions { },
) = navigate(route = route, navOptions)

private fun NavController.navigateToAlbumSearch() = navigate(route = AlbumSearchRoute)

fun NavGraphBuilder.overviewGraph(
    navController: NavController,
    navigateToSettings: () -> Unit,
    openNotificationsRequested: Boolean,
    onNotificationsOpened: () -> Unit,
) {
    val navigateToAlbumDetails: (AlbumDetailsRoute) -> Unit = navController::navigateToAlbumDetails
    navigation<OverviewBaseRoute>(startDestination = OverviewRoute) {
        sharedTransitionComposable<OverviewRoute> {
            OverviewRoute(
                navigateToSettings = navigateToSettings,
                navigateToSearch = navController::navigateToAlbumSearch,
                navigateToAlbumDetails = navigateToAlbumDetails,
                openNotificationsRequested = openNotificationsRequested,
                onNotificationsOpened = onNotificationsOpened,
            )
        }
        sharedTransitionComposable<AlbumSearchRoute> {
            AlbumSearchRoute(
                onNavigateBack = navController::navigateUp,
                navigateToAlbumDetails = navigateToAlbumDetails,
            )
        }
        sharedTransitionComposable<AlbumDetailsRoute> {
            val uriHandler = LocalUriHandler.current

            AlbumDetailsScreen(
                navigateToDetails = navigateToAlbumDetails,
                onNavigateBack = navController::navigateUp,
                openLink = { uriHandler.openUri(it) },
            )
        }
    }
}
