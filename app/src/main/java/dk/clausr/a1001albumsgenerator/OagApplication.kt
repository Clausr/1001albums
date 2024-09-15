package dk.clausr.a1001albumsgenerator

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import dk.clausr.a1001albumsgenerator.network.BuildConfig
import dk.clausr.worker.PeriodicProjectUpdateWidgetWorker
import io.sentry.SentryLevel
import io.sentry.android.core.SentryAndroid
import io.sentry.android.timber.SentryTimberIntegration
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class OagApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()

        startPeriodicWorker()

        initTimberAndSentry()

        SingletonImageLoader.setSafe {
            imageLoader
        }
    }

    private fun startPeriodicWorker() {
        PeriodicProjectUpdateWidgetWorker.start(this)
    }

    private fun initTimberAndSentry() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            SentryAndroid.init(this) { options ->
                options.addIntegration(
                    SentryTimberIntegration(
                        minEventLevel = SentryLevel.ERROR,
                        minBreadcrumbLevel = SentryLevel.INFO,
                    ),
                )
            }
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration = Configuration.Builder()
        .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
        .setMinimumLoggingLevel(android.util.Log.DEBUG)
        .build()
}
