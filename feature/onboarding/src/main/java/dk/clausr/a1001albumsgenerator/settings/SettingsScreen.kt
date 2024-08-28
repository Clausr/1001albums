package dk.clausr.a1001albumsgenerator.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dk.clausr.a1001albumsgenerator.onboarding.components.ProjectTextField
import dk.clausr.a1001albumsgenerator.onboarding.screens.StreamingServiceScreen
import dk.clausr.a1001albumsgenerator.ui.components.covergrid.CoverGrid
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.android.openLink
import dk.clausr.core.common.extensions.collectWithLifecycle
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.core.ui.CoverData
import timber.log.Timber

@Composable
fun SettingsRoute(
    onNavigateUp: () -> Unit,
    onClickApply: () -> Unit,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val projectId by viewModel.projectId.collectAsState()
    val preferredStreamingPlatform by viewModel.streamingPlatform.collectAsState()
    val covers by viewModel.coverData.collectAsState()

    var error: NetworkError? by remember {
        mutableStateOf(null)
    }

    viewModel.viewEffect.collectWithLifecycle {
        Timber.v("Handle viewEffect: $it")
        when (it) {
            is SettingsViewEffect.Error -> error = it.error
            SettingsViewEffect.ProjectSet -> error = null
        }
    }
    SettingsScreen(
        modifier = modifier,
        onNavigateUp = onNavigateUp,
        preferredStreamingPlatform = preferredStreamingPlatform,
        projectId = projectId,
        onSetStreamingPlatform = viewModel::setStreamingPlatform,
        onSetProjectId = viewModel::setProjectId,
        onClickApply = {
            onClickApply()
            viewModel.markOnboardingAsCompleted()
        },
        showBack = showBack,
        coverData = covers,
        error = error,
    )
}

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    preferredStreamingPlatform: StreamingPlatform?,
    projectId: String?,
    onSetStreamingPlatform: (StreamingPlatform) -> Unit,
    onSetProjectId: (String) -> Unit,
    onClickApply: () -> Unit,
    showBack: Boolean,
    error: NetworkError?,
    modifier: Modifier = Modifier,
    coverData: CoverData = CoverData.default(),
) {
    val hazeState = remember { HazeState() }
    var hideContent by remember { mutableStateOf(false) }
    val hideContentAlpha by animateFloatAsState(targetValue = if (hideContent) 0f else 1f, label = "Hide content alpha")

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    if (showBack) {
                        IconButton(
                            onClick = onNavigateUp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .hazeChild(
                                    state = hazeState,
                                    style = HazeStyle(
                                        backgroundColor = MaterialTheme.colorScheme.background,
                                        tint = HazeTint.Color(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                                    ),
                                ),
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { hideContent = !hideContent },
                        modifier = Modifier
                            .clip(CircleShape)
                            .hazeChild(
                                state = hazeState,
                                style = HazeStyle(
                                    backgroundColor = MaterialTheme.colorScheme.background,
                                    tint = HazeTint.Color(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                                ),
                            ),
                    ) {
                        if (hideContent) {
                            Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = "Visibility ")
                        } else {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = "Visibility ")
                        }
                    }
                },
            )
        },
        bottomBar = {
            val context = LocalContext.current
            if (showBack) {
                Row(
                    modifier = Modifier
                        .alpha(hideContentAlpha)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(onClick = { context.openLink("https://www.clausr.dk/privacy") }) {
                        Text("Privacy policy")
                    }
                }
            }
        },
    ) {
        Box(
            modifier = Modifier,
        ) {
            CoverGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                covers = coverData,
            )

            Column(
                modifier = Modifier
                    .alpha(hideContentAlpha)
                    .padding(it)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                ProjectTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = MaterialTheme.shapes.medium)
                        .hazeChild(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = MaterialTheme.colorScheme.background,
                                tint = HazeTint.Color(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                            ),
                        )
                        .padding(16.dp),
                    onProjectIdChange = onSetProjectId,
                    existingProjectId = projectId.orEmpty(),
                    error = error,
                )

                StreamingServiceScreen(
                    modifier = Modifier
                        .clip(shape = MaterialTheme.shapes.medium)
                        .hazeChild(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = MaterialTheme.colorScheme.background,
                                tint = HazeTint.Color(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                            ),
                        )
                        .padding(16.dp),
                    onSetStreamingPlatform = onSetStreamingPlatform,
                    preselectedPlatform = preferredStreamingPlatform,
                    showSelectButton = false,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = {
                            onClickApply()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        modifier = Modifier
                            .clip(shape = CircleShape)
                            .hazeChild(
                                state = hazeState,
                                style = HazeStyle(
                                    backgroundColor = MaterialTheme.colorScheme.background,
                                    tint = HazeTint.Color(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                                ),
                            ),
                    ) {
                        Text(text = "Done", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    OagTheme {
        SettingsScreen(
            onNavigateUp = {},
            preferredStreamingPlatform = null,
            projectId = null,
            onSetStreamingPlatform = {},
            onSetProjectId = {},
            onClickApply = {},
            showBack = true,
            error = null,
        )
    }
}
