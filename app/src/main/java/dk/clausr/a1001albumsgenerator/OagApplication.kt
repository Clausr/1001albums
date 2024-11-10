package dk.clausr.a1001albumsgenerator

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
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
import dk.clausr.a1001albumsgenerator.utils.CrashlyticsHelper
import dk.clausr.a1001albumsgenerator.utils.RoomLoggingTree
import dk.clausr.core.common.network.di.ApplicationCoroutineScope
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.worker.PeriodicProjectUpdateWidgetWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class OagApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var oagRepository: OagRepository

    @Inject
    @ApplicationCoroutineScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var roomLoggingTree: RoomLoggingTree

    private val usageStatsService by lazy { getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }
    override fun onCreate() {
        super.onCreate()

        startPeriodicWorker()

        initTimber()

        SingletonImageLoader.setSafe {
            imageLoader
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val standbyBucket = when (usageStatsService.appStandbyBucket) {
                UsageStatsManager.STANDBY_BUCKET_ACTIVE -> "Active"
                UsageStatsManager.STANDBY_BUCKET_RARE -> "Rare"
                UsageStatsManager.STANDBY_BUCKET_RESTRICTED -> "Restricted"
                UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> "Working set"
                UsageStatsManager.STANDBY_BUCKET_FREQUENT -> "Frequent"
                else -> ""
            }
            Timber.i("Standby bucket: $standbyBucket")
        }
    }

    private fun startPeriodicWorker() = applicationScope.launch {
        oagRepository.projectId.collectLatest {
            it?.let {
                PeriodicProjectUpdateWidgetWorker.start(this@OagApplication)
            }
        }
    }

    private fun initTimber() {
        Timber.plant(roomLoggingTree)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsHelper.CrashlyticsLoggingTree())
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
        .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.VERBOSE else android.util.Log.INFO)
        .build()
}
