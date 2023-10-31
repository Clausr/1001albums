package dk.clausr.a1001albumsgenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.a1001albumsgenerator.ui.theme._1001AlbumsGeneratorTheme
import dk.clausr.feature.overview.OverviewRoute
import dk.clausr.widget.DailyAlbumWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            DailyAlbumWidget().updateAll(applicationContext)
        }

        setContent {
            _1001AlbumsGeneratorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OverviewRoute()
                }
            }
        }
    }
}
