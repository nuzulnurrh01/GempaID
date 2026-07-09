package com.gempa.id.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property untuk membuat DataStore instance — singleton per Context
private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "user_preferences")

/**
 * ThemeDataStore — Mengelola preferensi tema (Dark/Light mode) pengguna.
 *
 * DataStore (Jetpack) adalah pengganti SharedPreferences yang lebih modern:
 * - Asynchronous (berbasis Coroutines & Flow) — tidak memblokir UI thread
 * - Type-safe dengan typed keys
 * - Konsisten dan aman dari race condition
 *
 * Data disimpan secara persisten — tema tidak reset saat app ditutup.
 */
@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Key untuk menyimpan preferensi dark mode
    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    /**
     * Flow yang mengamati preferensi dark mode.
     * Akan emit nilai baru setiap kali preferensi berubah.
     * Default: false (Light mode)
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false
        }

    /**
     * Menyimpan preferensi dark mode.
     * suspend function — aman dipanggil dari ViewModel/Coroutine.
     */
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    /**
     * Toggle tema — jika Light menjadi Dark, vice versa.
     */
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.IS_DARK_MODE] ?: false
            preferences[PreferencesKeys.IS_DARK_MODE] = !current
        }
    }
}
