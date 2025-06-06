package dk.clausr.a1001albumsgenerator.network.di

import android.content.Context
import android.os.storage.StorageManager
import androidx.core.content.getSystemService
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.size.Precision
import coil3.util.DebugLogger
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dk.clausr.a1001albumsgenerator.network.BuildConfig
import dk.clausr.a1001albumsgenerator.network.fake.FakeAssetManager
import dk.clausr.a1001albumsgenerator.network.interceptors.UserAgentInterceptor
import dk.clausr.a1001albumsgenerator.utils.InstantSerializer
import dk.clausr.core.common.network.AppInformation
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import okhttp3.Cache
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
        // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#lenient-parsing
        isLenient = true
        serializersModule = SerializersModule {
            contextual(Instant::class, InstantSerializer)
        }
    }

    @Provides
    @Singleton
    fun providesMockAssetManager(@ApplicationContext context: Context): FakeAssetManager = FakeAssetManager(context.assets::open)

    @Provides
    @Singleton
    fun provideOkHttpCache(@ApplicationContext context: Context): Cache {
        val defaultCacheSize: Long = 50 * 1024 * 1024 // 50 MiB
        val storageManager = context.getSystemService<StorageManager>()
        val cacheSize = storageManager?.getCacheQuotaBytes(storageManager.getUuidForPath(context.cacheDir)) ?: defaultCacheSize

        return Cache(context.cacheDir.resolve("http_cache"), cacheSize.coerceAtMost(defaultCacheSize))
    }

    @Provides
    @Singleton
    fun okHttpCallFactory(
        appInformation: AppInformation,
        cache: Cache,
    ): Call.Factory {
        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(timeout = 15, TimeUnit.SECONDS)
            .connectTimeout(timeout = 30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addNetworkInterceptor(
                        HttpLoggingInterceptor { message -> Timber.v(message) }
                            .apply {
                                level = HttpLoggingInterceptor.Level.BODY
                            },
                    )
                }
            }
            .addInterceptor(
                UserAgentInterceptor(appInformation),
            )
            .build()

        return okHttpClient
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        networkJson: Json,
        okhttpCallFactory: dagger.Lazy<Call.Factory>,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_URL)
        // We use callFactory lambda here with dagger.Lazy<Call.Factory>
        // to prevent initializing OkHttp on the main thread.
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun imageLoader(
        okHttpCallFactory: Call.Factory,
        @ApplicationContext application: Context,
        @Dispatcher(OagDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    ): ImageLoader = ImageLoader.Builder(application)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache(
            MemoryCache.Builder()
                .maxSizePercent(percent = 0.25, context = application)
                .build(),
        )
        .diskCachePolicy(CachePolicy.ENABLED)
        .diskCache {
            DiskCache
                .Builder()
                .directory(application.cacheDir.resolve("image_cache"))
                .maxSizePercent(percent = 0.05)
                .build()
        }
        .components { add(OkHttpNetworkFetcherFactory(okHttpCallFactory)) }
        .coroutineContext(ioDispatcher)
        .crossfade(true)
        .logger(if (BuildConfig.DEBUG) DebugLogger() else null)
        .precision(Precision.INEXACT)
        .build()
}
