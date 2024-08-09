package dk.clausr.feature.overview.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.android.require
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.feature.overview.OverviewDirections
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumDetailsViewModel @Inject constructor(
    oagRepository: OagRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val albumSlug by savedStateHandle.require<String>(OverviewDirections.Args.ALBUM_SLUG)

    val album = oagRepository.getHistoricAlbum(albumSlug).map {
        AlbumDetailsViewState.Success(it)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlbumDetailsViewState.Loading,
        )

    sealed interface AlbumDetailsViewState {
        data object Loading : AlbumDetailsViewState
        data class Success(val album: HistoricAlbum) : AlbumDetailsViewState
    }
}