package com.gempa.id

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * GempaApplication — Application class utama.
 *
 * @HiltAndroidApp: wajib untuk mengaktifkan Hilt DI di seluruh aplikasi.
 * Hilt akan men-generate komponen DI pada compile time — zero reflection overhead.
 *
 * Timber: logging library yang superior dibanding Log.d() bawaan Android.
 * Pada release build, semua log secara otomatis di-strip oleh ProGuard.
 */
@HiltAndroidApp
class GempaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Timber hanya pada debug build.
        // Pada release, tidak ada log yang keluar — keamanan & performa terjaga.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
