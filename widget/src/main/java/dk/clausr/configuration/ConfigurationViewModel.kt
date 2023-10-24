package dk.clausr.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val oagRepository: OagRepository
) : ViewModel() {


    private val _groupId: MutableStateFlow<String?> = MutableStateFlow(null)

    val groupFlow: StateFlow<Group?> = _groupId.mapNotNull { it }
        .flatMapLatest {
            oagRepository.getGroup(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    fun setGroupId(groupId: String) = viewModelScope.launch {
        _groupId.emit(groupId)
    }

//    init {
//        getGroup()
//    }

    fun getGroup() {
        Timber.d("Get group!")
        viewModelScope.launch {
            oagRepository.getGroup("claus-rasmus-delemusik")
//                .mapLatest {
//                    groupFlow.emit(it)
//                }
        }
    }
}
