package dk.clausr.feature.artistdetails

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
data class ArtistDetailsRoute(
    val artistId: String,
    val sharedItemKey: String,
)

fun NavController.navigateToArtistDetails(
    artistId: String,
    sharedItemKey: String,
    navOptions: NavOptions = androidx.navigation.navOptions { },
) = navigate(
    route = ArtistDetailsRoute(
        artistId = artistId,
        sharedItemKey = sharedItemKey
    ),
    navOptions = navOptions
)
