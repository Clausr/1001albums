package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.feature.onboarding.R
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme

@Composable
internal fun ProjectScreen(
    modifier: Modifier = Modifier,
    goBack: () -> Unit,
    navigateToMainApp: () -> Unit,
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
                        Button(onClick = {}, colors = ButtonDefaults.textButtonColors()) {
                            Text(text = stringResource(id = R.string.common_previous))
                        }
                        Button(onClick = navigateToMainApp) {
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

            Button(onClick = { /*TODO*/ }) {
                Text(stringResource(id = R.string.create_user))
                Spacer(Modifier.width(4.dp))
                Icon(painterResource(id = dk.clausr.a1001albumsgenerator.ui.R.drawable.ic_open_external), contentDescription = null)
            }

            Text("then enter")

            TextField(value = "", onValueChange = {}, placeholder = { Text("Username") })
        }
    }
}

@Preview
@Composable
private fun ProjectScreenPreview() {
    OagTheme {
        ProjectScreen(
            goBack = {},
            navigateToMainApp = {},
        )
    }
}