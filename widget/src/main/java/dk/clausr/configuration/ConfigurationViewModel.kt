package dk.clausr.configuration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.clausr.worker.PeriodicProjectUpdateWidgetWorker
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    fun updateWidgets() = viewModelScope.launch {
        PeriodicProjectUpdateWidgetWorker.start(context)
    }
}
