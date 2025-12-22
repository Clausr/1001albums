package dk.clausr.a1001albumsgenerator.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.clausr.a1001albumsgenerator.settings.log.LogViewModel
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
import dk.clausr.core.data.model.log.OagLog

@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = hiltViewModel(),
) {
    TrackScreenViewEvent(screenName = "LogScreen")
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(logs) { log ->
                val logColor = log.getColor()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(logColor.copy(alpha = 0.2f)),
                ) {
                    Box(
                        Modifier
                            .width(width = 20.dp)
                            .background(logColor)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = log.level.name.first().toString(),
                            color = contentColorFor(logColor),
                        )
                    }

                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = log.dateTime,
                            fontSize = 10.sp,
                        )

                        Text(text = log.message, fontSize = 14.sp)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun OagLog.getColor(): Color = Color(
    color = when (level) {
        OagLog.LogLevel.VERBOSE -> 0xFFD6D6D6
        OagLog.LogLevel.DEBUG -> 0xFF0091EA
        OagLog.LogLevel.INFO -> 0xFF6A8759
        OagLog.LogLevel.WARN -> 0xFFFFAB40
        OagLog.LogLevel.ERROR -> 0xFFDD2C00
        OagLog.LogLevel.ASSERT -> 0xFFFF6B68
    },
)
