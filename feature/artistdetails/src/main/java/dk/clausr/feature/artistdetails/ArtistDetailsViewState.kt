package dk.clausr.feature.artistdetails

import dk.clausr.core.model.HistoricAlbum
import kotlinx.collections.immutable.PersistentList

data class ArtistDetailsViewState(
    val artistName: String,
    val albums: PersistentList<HistoricAlbum>,
    val averagePersonalRating: Float? = null,
    val averageGroupRating: Float? = null,
    val isLoading: Boolean = true,
)
