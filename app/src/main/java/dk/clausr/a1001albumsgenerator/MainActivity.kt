package dk.clausr.a1001albumsgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.a1001albumsgenerator.ui.OagApp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        var uiState: MainViewState by mutableStateOf(MainViewState.Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        setContent {
            val navHostController = rememberNavController()

            OagApp(
                uiState = uiState,
                navHostController = navHostController,
            )

//
//            _1001AlbumsGeneratorTheme {
//
//                val uiState by viewModel.uiState.collectAsState()
//                Scaffold(
//                    topBar = {
//                        TopAppBar(title = { Text(text = "1001 albums") },
//                            actions = {
//                                IconButton(onClick = {
//
//                                }) {
//                                    Icon(
//                                        imageVector = Icons.Default.Settings,
//                                        contentDescription = "Configure project",
//                                    )
//                                }
//                            })
//                    },
//                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
//                ) { innerPadding ->
//                    Surface(
//                        modifier = Modifier
//                            .padding(innerPadding)
//                            .consumeWindowInsets(innerPadding),
//                    ) {
//                        when (uiState) {
//                            is MainViewState.HasProject -> {
//                                Column(
//                                    Modifier
//                                        .statusBarsPadding()
//                                        .fillMaxSize()
//                                ) {
//                                    Box(
//                                        Modifier
//                                            .fillMaxWidth()
//                                            .aspectRatio(1f)
//                                    ) {
//                                        WidgetView(widget = AlbumCoverWidget())
//                                    }
//                                    OverviewRoute(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .fillMaxHeight(),
//                                    )
//                                }
//                            }
//
//                            MainViewState.Loading -> {
//                                Box(
//                                    modifier = Modifier.fillMaxSize(),
//                                    contentAlignment = Alignment.Center,
//                                ) {
//                                    CircularProgressIndicator()
//                                }
//                            }
//
//                            MainViewState.NoProject -> {
//                                WidgetConfigurationRoute(
//                                    onUpClicked = { /*TODO*/ },
//                                    onProjectIdSet = { /*TODO*/ },
//                                    onApplyChanges = { /*TODO*/ })
//                            }
//                        }
//                    }
//                }
//            }
        }
    }
}
