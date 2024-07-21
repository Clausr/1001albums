package dk.clausr.a1001albumsgenerator.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dk.clausr.a1001albumsgenerator.onboarding.components.childHazeModifier
import dk.clausr.a1001albumsgenerator.onboarding.screens.ProjectNameScreen
import dk.clausr.a1001albumsgenerator.onboarding.screens.StreamingServiceScreen
import dk.clausr.a1001albumsgenerator.onboarding.screens.SummaryScreen
import dk.clausr.a1001albumsgenerator.ui.components.OagNavHost
import dk.clausr.a1001albumsgenerator.ui.components.covergrid.CoverGrid
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.extensions.collectWithLifecycle
import dk.clausr.core.model.StreamingPlatform

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingScreenViewModel = hiltViewModel(),
) {
    val internalNavController = rememberNavController()

    var error: String? by remember {
        mutableStateOf(null)
    }

    val projectId by viewModel.projectId.collectAsState(initial = null)
    val streamingPlatform by viewModel.streamingPlatform.collectAsState(initial = null)

    viewModel.viewEffect.collectWithLifecycle {
        when (it) {
            IntroViewEffects.ProjectNotFound -> {
                error = "Project not found, try to create one!"
            }

            IntroViewEffects.ProjectSet -> {
                internalNavController.navigate("streamingService")
            }

            IntroViewEffects.StreamingServiceSet -> {
                internalNavController.navigate("summary")
            }

            IntroViewEffects.OnboardingDone -> {
                viewModel.markIntroFlowAsCompleted()
            }
        }
    }

    OnboardingScreen(
        navHostController = internalNavController,
        onSetProjectId = viewModel::setProjectId,
        modifier = modifier.fillMaxSize(),
        error = error,
        onSetStreamingPlatform = viewModel::setStreamingPlatform,
        onDone = viewModel::markIntroFlowAsCompleted,
        projectId = projectId,
        preferredStreamingPlatform = streamingPlatform,
    )
}

@Composable
internal fun OnboardingScreen(
    projectId: String?,
    preferredStreamingPlatform: StreamingPlatform?,
    error: String?,
    onSetProjectId: (String) -> Unit,
    onSetStreamingPlatform: (StreamingPlatform) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
) {
    val hazeState = remember { HazeState() }

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
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            OagNavHost(
                navController = navHostController,
                startDestination = "intro",
            ) {
                composable("intro") {
                    ProjectNameScreen(
                        modifier = Modifier.childHazeModifier(hazeState),
                        prefilledProjectId = projectId.orEmpty(),
                        onSetProjectId = onSetProjectId,
                        error = error,
                    )
                }

                composable(route = "streamingService") {
                    StreamingServiceScreen(
                        modifier = Modifier.childHazeModifier(hazeState),
                        onSetStreamingPlatform = onSetStreamingPlatform,
                        preselectedPlatform = preferredStreamingPlatform,
                    )
                }

                composable(route = "summary") {
                    SummaryScreen(
                        modifier = Modifier.childHazeModifier(hazeState),
                        onDone = onDone,
                    )

                }
            }
        }
    }
}


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
            projectId = null,
            preferredStreamingPlatform = null,
        )
    }
}
