package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.onboarding.components.OnboardingTitle
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.android.openLink
import kotlinx.coroutines.launch

@Composable
internal fun ProjectNameScreen(
    onSetProjectId: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefilledProjectId: String = "",
    error: String? = null,
    onProjectIdChanged: (String) -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var projectId: String by remember(prefilledProjectId) {
        mutableStateOf(prefilledProjectId)
    }

    var isError: Boolean by remember(error) {
        mutableStateOf(error != null)
    }

    val setProjectButtonEnabled by remember(projectId, error) {
        mutableStateOf(projectId.isNotBlank() && !isError)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingTitle("1001 Albums Generator")

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
                onProjectIdChanged(it)
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

@Preview
@Composable
private fun ProjectScreenPreview() {
    OagTheme {
        ProjectNameScreen(
//            navigateUp = {},
            onSetProjectId = {},
        )
    }
}
