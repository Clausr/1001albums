package dk.clausr.a1001albumsgenerator.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.onboarding.screens.ProjectContent
import dk.clausr.a1001albumsgenerator.ui.components.OagNavHost
import dk.clausr.a1001albumsgenerator.ui.components.covergrid.CoverGrid
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.extensions.collectWithLifecycle
import dk.clausr.core.model.StreamingPlatform
import timber.log.Timber

@Composable
internal fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingScreenViewModel = hiltViewModel(),
) {
    var error: String? by remember {
        mutableStateOf(null)
    }

    val viewState by viewModel.viewState.collectAsState()

    viewModel.viewEffect.collectWithLifecycle {
        when (it) {
            IntroViewEffects.ProjectNotFound -> error = "Project not found, try to create one!"
        }
    }

    OnboardingScreen(
        onSetProjectId = viewModel::setProjectId,
        modifier = modifier.fillMaxSize(),
        error = error,
        onSetStreamingPlatform = viewModel::setStreamingPlatform,
        viewState = viewState,
        onDone = viewModel::markIntroFlowAsCompleted,
    )
}

@Composable
internal fun OnboardingScreen(
    error: String?,
    onSetProjectId: (String) -> Unit,
    onSetStreamingPlatform: (StreamingPlatform) -> Unit,
    onDone: () -> Unit,
    viewState: IntroViewState,
    modifier: Modifier = Modifier,
) {
    val hazeState = remember { HazeState() }
    val internalNavController = rememberNavController()

    LaunchedEffect(viewState) {
        when (viewState) {
            IntroViewState.Initial -> internalNavController.navigate("intro") {
                popUpTo("intro") {
                    inclusive = true
                }
            }

            is IntroViewState.ProjectSet -> internalNavController.navigate("streamingService")
            is IntroViewState.StreamingServiceSet -> internalNavController.navigate("summary")
            is IntroViewState.Done -> Timber.d("Done")
        }
    }

    Box(
        modifier = modifier,
    ) {
        CoverGrid(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState),
            rowCount = 8,
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            OagNavHost(
                navController = internalNavController,
                startDestination = "intro",
            ) {
                composable("intro") {
                    Column(modifier = Modifier.childModifier(hazeState)) {
                        Title("1001 Albums Generator")
                        ProjectContent(
                            onSetProjectId = onSetProjectId,
                            error = error,
                        )
                    }
                }

                composable(route = "streamingService") {
                    Column(
                        modifier = Modifier.childModifier(hazeState),
                    ) {
                        Title(text = "Streaming service")

                        Text("Set streaming service")
                        Button(onClick = { onSetStreamingPlatform(StreamingPlatform.Tidal) }) {
                            Text("Click me")
                        }
                    }
                }

                composable(route = "summary") {
                    Column(
                        modifier = Modifier.childModifier(hazeState),
                    ) {
                        Title(text = stringResource(id = R.string.all_set))

                        Text("Hello")
                        Button(onClick = { onDone() }) {
                            Text(text = stringResource(id = R.string.lets_go))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Title(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = text,
        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    )
}

@Suppress("ModifierComposable") // TODO Do this in Modifier.Node?
@Composable
private fun Modifier.childModifier(hazeState: HazeState) = Modifier
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

@Preview
@Composable
private fun AppScreenPreview() {
    OagTheme {
        OnboardingScreen(
            modifier = Modifier.fillMaxSize(),
            onSetProjectId = {},
            error = null,
            onDone = {},
            onSetStreamingPlatform = {},
            viewState = IntroViewState.Initial,
        )
    }
}
