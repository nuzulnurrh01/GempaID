package com.gempa.id.core.di

import android.content.Context
import androidx.room.Room
import com.gempa.id.BuildConfig
import com.gempa.id.core.data.local.GempaDatabase
import com.gempa.id.core.data.local.dao.GempaDao
import com.gempa.id.core.data.remote.api.BmkgApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * AppModule — Hilt DI Module untuk menyediakan dependency aplikasi.
 *
 * @Module: menandai class ini sebagai penyedia dependency untuk Hilt.
 * @InstallIn(SingletonComponent::class): semua dependency di sini adalah
 * Application-scoped (hidup selama app berjalan) — ini adalah scope yang
 * tepat untuk networking dan database.
 *
 * Hilt akan menginjeksi dependency ini ke mana saja yang membutuhkannya
 * hanya dengan anotasi @Inject.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Network Layer ────────────────────────────────────────────────────────

    /**
     * Konfigurasi KotlinX Serialization JSON parser.
     * ignoreUnknownKeys = true: toleran terhadap field baru dari API
     * tanpa crash aplikasi — defensive programming.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues  = true  // null → default value, bukan crash
        isLenient          = true  // toleran terhadap JSON yang kurang ketat
    }

    /**
     * OkHttpClient dengan konfigurasi timeout dan logging interceptor.
     *
     * Logging Interceptor hanya aktif di DEBUG build — pada RELEASE,
     * tidak ada data sensitif yang masuk ke log.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            timber.log.Timber.tag("OkHttp").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)  // timeout koneksi
            .readTimeout(30, TimeUnit.SECONDS)     // timeout baca data
            .writeTimeout(30, TimeUnit.SECONDS)    // timeout kirim data
            .build()
    }

    /**
     * Retrofit instance — HTTP client yang akan men-generate
     * implementasi BmkgApiService secara otomatis.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BMKG_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * Retrofit akan men-generate implementasi BmkgApiService.
     * Setiap pemanggilan suspend function di interface akan dikonversi
     * menjadi HTTP request secara otomatis.
     */
    @Provides
    @Singleton
    fun provideBmkgApiService(retrofit: Retrofit): BmkgApiService =
        retrofit.create(BmkgApiService::class.java)

    // ── Database Layer ───────────────────────────────────────────────────────

    /**
     * Room Database instance.
     * fallbackToDestructiveMigration: untuk development.
     * Pada production, ganti dengan Migration objects yang proper.
     */
    @Provides
    @Singleton
    fun provideGempaDatabase(
        @ApplicationContext context: Context
    ): GempaDatabase = Room.databaseBuilder(
        context,
        GempaDatabase::class.java,
        GempaDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()

    /**
     * GempaDao disediakan dari database instance.
     */
    @Provides
    @Singleton
    fun provideGempaDao(database: GempaDatabase): GempaDao =
        database.gempaDao()
}
