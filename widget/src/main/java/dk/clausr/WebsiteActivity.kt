package dk.clausr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.core.data.repository.OagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WebsiteActivity : ComponentActivity() {

    @Inject
    lateinit var repo: OagRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Timber.d("Start activity - ${intent.extras?.getString("Hej")}")
        CoroutineScope(Dispatchers.IO).launch {
            repo.projectId.collectLatest {
                Timber.d("Collected $it")
                launch(Dispatchers.Main) {
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://1001albumsgenerator.com/$it")
                        }
                    )
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()


    }
}
