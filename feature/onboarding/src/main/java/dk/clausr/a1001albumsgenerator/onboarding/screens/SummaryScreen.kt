package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.onboarding.components.OnboardingTitle
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme

@Composable
internal fun SummaryScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingTitle(text = stringResource(id = R.string.all_set))

        Text(
            text = "Hello\n\t\t- Adele",
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = onNext) {
            Text(text = stringResource(id = R.string.lets_go))
        }
    }
}

@Preview
@Composable
private fun SummaryPreview() {
    OagTheme {
        SummaryScreen(onNext = {})
    }
}
