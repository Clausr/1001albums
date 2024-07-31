package dk.clausr.a1001albumsgenerator.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.stringResource
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import kotlinx.coroutines.launch

@Composable
fun ProjectTextField(
    onProjectIdChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    existingProjectId: String = "",
    error: String? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var projectId: String by remember(existingProjectId) {
        mutableStateOf(existingProjectId)
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
            Text(stringResource(R.string.username_textfield_label))
        },
        singleLine = true,
        value = projectId,
        keyboardActions = KeyboardActions(onDone = {
            scope.launch {
                keyboardController?.hide()
            }
        }),
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
            AnimatedVisibility(
                visible = setProjectButtonEnabled,
                enter = fadeIn(),
                exit = fadeOut(),
                label = "Icon animation",
            ) {
                IconButton(
                    onClick = {
                        onProjectIdChange(projectId)
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
            }
        },
    )
}
