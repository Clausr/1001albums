package dk.clausr.a1001albumsgenerator.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Adb
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsEvent
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.onboarding.components.ProjectTextField
import dk.clausr.a1001albumsgenerator.onboarding.screens.StreamingServiceScreen
import dk.clausr.a1001albumsgenerator.ui.components.covergrid.CoverGrid
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
import dk.clausr.a1001albumsgenerator.ui.extensions.logClickEvent
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.BuildConfig
import dk.clausr.core.common.ExternalLinks
import dk.clausr.core.common.extensions.openLink
import dk.clausr.core.model.StreamingPlatform

@Composable
fun SettingsRoute(
    onNavigateUp: () -> Unit,
    onClickApply: () -> Unit,
    onShowLogs: () -> Unit,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    TrackScreenViewEvent("SettingsScreen")
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        viewState = viewState,
        onNavigateUp = onNavigateUp,
        onSetStreamingPlatform = viewModel::setStreamingPlatform,
        onSetProjectId = viewModel::setProjectId,
        onClickApply = {
            onClickApply()
            viewModel.markOnboardingAsCompleted()
        },
        showBack = showBack,
        onShowLogs = onShowLogs,
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewState: SettingsViewModel.ViewState,
    onSetStreamingPlatform: (StreamingPlatform) -> Unit,
    onSetProjectId: (String) -> Unit,
    onClickApply: () -> Unit,
    onShowLogs: () -> Unit,
    showBack: Boolean,
    modifier: Modifier = Modifier,
) {
    val hazeState = remember { HazeState() }
    var hideContent by remember { mutableStateOf(false) }
    val hideContentAlpha by animateFloatAsState(targetValue = if (hideContent) 0f else 1f, label = "Hide content alpha")

    val analyticsHelper = LocalAnalyticsHelper.current

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
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.regular(),
                                ),
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (BuildConfig.DEBUG) {
                            // Button to check logs for the app
                            IconButton(
                                onClick = onShowLogs,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .hazeEffect(
                                        state = hazeState,
                                        style = HazeMaterials.regular(),
                                    ),
                            ) {
                                Icon(imageVector = Icons.Default.Adb, contentDescription = null)
                            }
                        }
                        IconButton(
                            onClick = { hideContent = !hideContent },
                            modifier = Modifier
                                .clip(CircleShape)
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.regular(),
                                )
                                .clearAndSetSemantics { invisibleToUser() },
                        ) {
                            if (hideContent) {
                                Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = "Visibility ")
                            } else {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = "Visibility ")
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            val context = LocalContext.current
            if (showBack) {
                Column(
                    modifier = Modifier
                        .alpha(hideContentAlpha)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(onClick = {
                        context.openLink(ExternalLinks.Clausr.PRIVACY_POLICY)
                        analyticsHelper.logClickEvent("Privacy policy")
                    }) {
                        Text(stringResource(R.string.privacy_policy_title))
                    }
                    Text(
                        text = viewState.buildVersion,
                        modifier = Modifier.combinedClickable(
                            onLongClick = onShowLogs,
                            interactionSource = null,
                            indication = null,
                            onClick = {},
                        ),
                    )
                }
            }
        },
    ) {
        Box {
            CoverGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                covers = viewState.covers,
            )

            Column(
                modifier = Modifier
                    .alpha(hideContentAlpha)
                    .padding(it)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                if (!showBack) {
                    ProjectTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = MaterialTheme.shapes.medium)
                            .hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.ultraThin(),
                            )
                            .padding(16.dp),
                        enabled = viewState.editProjectIdEnabled,
                        onProjectIdChange = {
                            analyticsHelper.logEvent(
                                AnalyticsEvent(
                                    type = AnalyticsEvent.Types.CLICK_ITEM,
                                    extras = listOf(
                                        AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.EVENT_NAME, "Set project"),
                                        AnalyticsEvent.Param("is_not_blank", it.isNotBlank().toString()),
                                        AnalyticsEvent.Param("edit_project_enabled", viewState.editProjectIdEnabled.toString()),
                                    ),
                                ),
                            )

                            onSetProjectId(it)
                        },
                        existingProjectId = viewState.projectId.orEmpty(),
                        error = viewState.error,
                    )
                }

                StreamingServiceScreen(
                    modifier = Modifier
                        .clip(shape = MaterialTheme.shapes.medium)
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.ultraThin(),
                        )
                        .padding(16.dp),
                    onSetStreamingPlatform = {
                        onSetStreamingPlatform(it)
                    },
                    preselectedPlatform = viewState.preferredStreamingPlatform,
                    showSelectButton = false,
                )

                if (!showBack) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            onClick = {
                                onClickApply()
                                analyticsHelper.logClickEvent("Done")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                            modifier = Modifier
                                .clip(shape = CircleShape)
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.regular(),
                                ),
                        ) {
                            Text(text = "Done", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
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
            onSetStreamingPlatform = {},
            onSetProjectId = {},
            onClickApply = {},
            showBack = true,
            viewState = SettingsViewModel.ViewState(
                projectId = "OagUser",
                preferredStreamingPlatform = StreamingPlatform.Spotify,
                buildVersion = "1.3.3.7",
            ),
            onShowLogs = {},
        )
    }
}
