package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme

@Composable
internal fun WidgetScreen(
    navigateUp: () -> Unit,
    navigateNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        Button(onClick = navigateNext) {
                            Text(text = stringResource(id = R.string.lets_go))
                        }
                    }
                },
            )
        },
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.onboarding_title_widget)) })
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp),
        ) {
            Text(text = stringResource(id = R.string.onboarding_description_widget))
        }
    }
}

@Preview
@Composable
private fun AppScreenPreview() {
    OagTheme {
        WidgetScreen(
            navigateUp = {},
            navigateNext = {},
        )
    }
}
