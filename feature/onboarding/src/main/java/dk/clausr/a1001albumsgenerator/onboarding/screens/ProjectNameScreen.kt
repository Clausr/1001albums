package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.onboarding.components.OnboardingTitle
import dk.clausr.a1001albumsgenerator.onboarding.components.ProjectTextField
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.android.openLink

@Composable
internal fun ProjectNameScreen(
    onSetProjectId: (String) -> Unit,
    modifier: Modifier = Modifier,
    prefilledProjectId: String = "",
    error: String? = null,
) {
    val context = LocalContext.current

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

        ProjectTextField(
            onProjectIdChange = onSetProjectId,
            modifier = Modifier.fillMaxWidth(),
            existingProjectId = prefilledProjectId,
            error = error,
        )
    }
}

@Preview
@Composable
private fun ProjectScreenPreview() {
    OagTheme {
        ProjectNameScreen(
            onSetProjectId = {},
        )
    }
}
