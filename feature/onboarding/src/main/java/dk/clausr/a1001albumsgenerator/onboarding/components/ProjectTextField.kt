package dk.clausr.a1001albumsgenerator.onboarding.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.launch

@Composable
fun ProjectTextField(
    onSetProjectId: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefilledProjectId: String = "",
    error: String? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var projectId: String by remember(prefilledProjectId) {
        mutableStateOf(prefilledProjectId)
    }

    var isError: Boolean by remember(error) {
        mutableStateOf(error != null)
    }

    val setProjectButtonEnabled by remember(projectId, error) {
        mutableStateOf(projectId.isNotBlank() && !isError)
    }

    TextField(
        modifier = modifier,
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
