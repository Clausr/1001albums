package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.android.openLink
import dk.clausr.core.common.extensions.collectWithLifecycle
import kotlinx.coroutines.launch

@Composable
internal fun ProjectRoute(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectViewModel = hiltViewModel(),
) {
    var error: String? by remember {
        mutableStateOf(null)
    }
    viewModel.viewEffect.collectWithLifecycle {
        when (it) {
            ProjectViewEffects.ProjectNotFound -> error = "Project not found"
        }
    }

    ProjectScreen(
        modifier = modifier,
        navigateUp = navigateUp,
        error = error,
        onSetProjectId = viewModel::setProjectId,
    )
}

@Composable
internal fun ProjectScreen(
    navigateUp: () -> Unit,
    onSetProjectId: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var projectId: String by remember {
        mutableStateOf("")
    }

    var isError: Boolean by remember(error) {
        mutableStateOf(error != null)
    }

    val setProjectButtonEnabled by remember(projectId, error) {
        mutableStateOf(projectId.isNotBlank() && !isError)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                    ) {
                        Button(onClick = navigateUp, colors = ButtonDefaults.textButtonColors()) {
                            Text(text = stringResource(id = R.string.common_previous))
                        }
                        Button(
                            onClick = { onSetProjectId(projectId) },
                            enabled = setProjectButtonEnabled,
                        ) {
                            Text(text = stringResource(id = R.string.all_set))
                        }
                    }
                },
            )
        },
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.onboarding_title_project)) })
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(id = R.string.onboarding_description_project))

            Button(onClick = { context.openLink("https://1001albumsgenerator.com/") }) {
                Text(stringResource(id = R.string.create_user))
                Spacer(Modifier.width(4.dp))
                Icon(painterResource(id = dk.clausr.a1001albumsgenerator.ui.R.drawable.ic_open_external), contentDescription = null)
            }

            Text("then enter it here:")

            TextField(
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Username")
                },
                singleLine = true,
                value = projectId,
                keyboardActions = KeyboardActions(onDone = { onSetProjectId(projectId) }),
                onValueChange = {
                    projectId = it
                    isError = false
                },
                isError = isError,
                supportingText = if (isError) {
                    error?.let { { Text(it) } }
                } else {
                    null
                },
                trailingIcon = {
                    AnimatedContent(
                        targetState = setProjectButtonEnabled,
                        label = "Icon animation",
                    ) { setProjectEnabled ->
                        if (setProjectEnabled) {
                            IconButton(
                                onClick = {
                                    onSetProjectId(projectId)
                                    scope.launch {
                                        keyboardController?.hide()
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    projectId = ""
                                    isError = false
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun ProjectScreenPreview() {
    OagTheme {
        ProjectScreen(
            navigateUp = {},
            onSetProjectId = {},
        )
    }
}
