package dk.clausr.a1001albumsgenerator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.UserRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userRepo: UserRepository,
) : ViewModel() {

    val uiState = userRepo.hasOnboarded.map {
        MainViewState.Success(hasOnboarded = it)
    }

    init {

    }
}

sealed interface MainViewState {
    data object Loading : MainViewState
    data class Success(val hasOnboarded: Boolean) : MainViewState
}
