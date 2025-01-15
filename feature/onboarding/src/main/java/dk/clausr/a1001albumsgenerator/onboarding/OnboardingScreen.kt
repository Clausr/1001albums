package dk.clausr.a1001albumsgenerator.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dk.clausr.a1001albumsgenerator.onboarding.screens.ProjectNameScreen
import dk.clausr.a1001albumsgenerator.onboarding.screens.StreamingServiceScreen
import dk.clausr.a1001albumsgenerator.ui.components.covergrid.CoverGrid
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.extensions.collectWithLifecycle
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingScreenViewModel = hiltViewModel(),
) {
    val internalNavController = rememberNavController()

    var error: NetworkError? by remember {
        mutableStateOf(null)
    }

    val projectId by viewModel.projectId.collectAsState(initial = null)
    val streamingPlatform by viewModel.streamingPlatform.collectAsState(initial = null)

    viewModel.viewEffect.collectWithLifecycle {
        when (it) {
            is IntroViewEffects.ProjectError -> {
                error = it.error
            }

            IntroViewEffects.ProjectSet -> {
                error = null
                internalNavController.navigate(OnboardingDirections.streamingPlatform())
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
        projectId = projectId,
        preferredStreamingPlatform = streamingPlatform,
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
internal fun OnboardingScreen(
    projectId: String?,
    preferredStreamingPlatform: StreamingPlatform?,
    error: NetworkError?,
    onSetProjectId: (String) -> Unit,
    onSetStreamingPlatform: (StreamingPlatform) -> Unit,
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
                .hazeSource(state = hazeState),
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            NavHost(
                navController = navHostController,
                startDestination = OnboardingDirections.Routes.ROOT,
            ) {
                navigation(
                    route = OnboardingDirections.Routes.ROOT,
                    startDestination = OnboardingDirections.projectName(),
                ) {
                    composable(route = OnboardingDirections.projectName()) {
                        ProjectNameScreen(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(shape = MaterialTheme.shapes.medium)
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.ultraThin(),
                                )
                                .padding(all = 16.dp),
                            prefilledProjectId = projectId.orEmpty(),
                            onSetProjectId = onSetProjectId,
                            error = error,
                        )
                    }

                    composable(route = OnboardingDirections.streamingPlatform()) {
                        StreamingServiceScreen(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(shape = MaterialTheme.shapes.medium)
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.ultraThin(),
                                )
                                .padding(all = 16.dp),
                            onSetStreamingPlatform = onSetStreamingPlatform,
                            preselectedPlatform = preferredStreamingPlatform,
                        )
                    }
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
            onSetStreamingPlatform = {},
            projectId = null,
            preferredStreamingPlatform = null,
        )
    }
}
