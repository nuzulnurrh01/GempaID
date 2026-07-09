package com.gempa.id

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gempa.id.core.data.local.datastore.ThemeDataStore
import com.gempa.id.ui.theme.GempaIDTheme
import com.gempa.id.core.navigation.GempaNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity — Satu-satunya Activity dalam aplikasi (Single Activity Architecture).
 *
 * Pola ini adalah best practice modern Android:
 * - Seluruh navigasi ditangani oleh Navigation Compose
 * - Activity hanya bertugas sebagai "container" untuk Compose UI
 * - @AndroidEntryPoint memungkinkan Hilt menginjeksi dependency ke sini
 *
 * enableEdgeToEdge(): konten meluas hingga ke area system bar (status bar & navigation bar)
 * untuk pengalaman visual yang lebih imersif — standar Material 3.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeDataStore: ThemeDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-Edge display — Material 3 design guideline
        enableEdgeToEdge()

        setContent {
            // Observasi preferensi tema dari DataStore.
            // collectAsStateWithLifecycle() lebih efisien dari collectAsState()
            // karena secara otomatis berhenti mengumpulkan ketika lifecycle tidak aktif.
            val isDarkTheme by themeDataStore.isDarkMode
                .collectAsStateWithLifecycle(initialValue = false)

            GempaIDTheme(darkTheme = isDarkTheme) {
                // NavHost mengelola seluruh navigation graph aplikasi
                GempaNavHost()
            }
        }
    }
}
