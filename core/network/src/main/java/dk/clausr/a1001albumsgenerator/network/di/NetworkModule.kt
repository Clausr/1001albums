package dk.clausr.a1001albumsgenerator.network.di

import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dk.clausr.a1001albumsgenerator.network.BuildConfig
import dk.clausr.a1001albumsgenerator.network.fake.FakeAssetManager
import dk.clausr.a1001albumsgenerator.utils.InstantSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
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
        // TODO Maybe remove this?
        // https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/json.md#lenient-parsing
        isLenient = true
        serializersModule = SerializersModule {
            contextual(Instant::class, InstantSerializer)
        }
    }

    @Provides
    @Singleton
    fun providesMockAssetManager(
        @ApplicationContext context: Context,
    ): FakeAssetManager = FakeAssetManager(context.assets::open)

    @Provides
    @Singleton
    fun okHttpCallFactory(

    ): Call.Factory {
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addNetworkInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()

        return okHttpClient
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        networkJson: Json,
        okhttpCallFactory: Call.Factory,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BACKEND_URL)
        .callFactory(okhttpCallFactory)
        .addConverterFactory(
            networkJson.asConverterFactory("application/json".toMediaType()),
        )
        .build()

    @Provides
    @Singleton
    fun imageLoader(
        okHttpCallFactory: Call.Factory,
        @ApplicationContext application: Context,
    ): ImageLoader = ImageLoader.Builder(application)
        .callFactory(okHttpCallFactory)
        // Assume most content images are versioned urls
        // but some problematic images are fetching each time
//        .respectCacheHeaders(false)
        .apply {
            if (BuildConfig.DEBUG) {
                logger(DebugLogger())
            }
        }
        .build()
}
