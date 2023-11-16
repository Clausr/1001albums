package dk.clausr.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.repository.OagRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext context: Context,
) {
    private val viewModelScope = CoroutineScope(ioDispatcher + SupervisorJob())
    private val refresh: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)

    val widgetState: Flow<WidgetState> = oagRepository.widget
        .onStart { WidgetState.Loading }
        .map { widgetModel ->
            when {
                widgetModel == null -> WidgetState.Error
                widgetModel.newAlbumAvailable -> WidgetState.RateYesterday(
                    projectId = widgetModel.projectName, widgetModel.currentCoverUrl
                )

                else -> {
                    WidgetState.TodaysAlbum(
                        coverUrl = widgetModel.currentCoverUrl,
                        artist = widgetModel.currentAlbumArtist,
                        album = widgetModel.currentAlbumTitle,
                        projectId = widgetModel.projectName,
                    )
                }
            }
        }
        .onCompletion { DailyAlbumWidget.updateAll(context) }

    init {
        viewModelScope.launch {
            refresh.emit(Unit)
        }
    }

    fun refresh() = CoroutineScope(ioDispatcher).launch {
        refresh.emit(Unit)
    }

}

sealed interface WidgetState {
    data object Loading : WidgetState
    data object Error : WidgetState
    data class RateYesterday(val projectId: String, val coverUrl: String) : WidgetState
    data class TodaysAlbum(
        val projectId: String,
        val coverUrl: String,
        val artist: String,
        val album: String
    ) : WidgetState

}
