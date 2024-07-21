package dk.clausr.a1001albumsgenerator.onboarding.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

@Suppress("ModifierComposable") // TODO Do this in Modifier.Node?
@Composable
internal fun Modifier.childHazeModifier(hazeState: HazeState) = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp)
    .hazeChild(
        state = hazeState,
        shape = MaterialTheme.shapes.medium,
        style = HazeStyle(
            backgroundColor = MaterialTheme.colorScheme.background,
            tint = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
        ),
    )
    .padding(all = 16.dp)
    .then(this)
