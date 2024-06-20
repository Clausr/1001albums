package dk.clausr.a1001albumsgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.a1001albumsgenerator.ui.theme._1001AlbumsGeneratorTheme
import dk.clausr.feature.overview.OverviewRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    val viewModel: MainActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            _1001AlbumsGeneratorTheme {

                val uiState by viewModel.uiState.collectAsState()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Crossfade(uiState, label = "") { state ->
                        when (state) {
                            is MainViewState.HasProject -> OverviewRoute(
                                modifier = Modifier
                            )

                            MainViewState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            MainViewState.NoProject -> {

                            }
                        }
                    }
                }
            }
        }
    }
}
