package com.gempa.id.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Material 3 Color Scheme — Light Mode
 * Menggunakan palet warna "Seismic Dashboard" yang didefinisikan di Color.kt
 */
private val LightColorScheme = lightColorScheme(
    primary            = OrangePrimary,
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = OrangeContainer,
    onPrimaryContainer = OnOrangeContainer,
    secondary          = BlueSecondary,
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = BlueContainer,
    onSecondaryContainer = OnBlueContainer,
    tertiary           = GreenTertiary,
    onTertiary         = Color(0xFFFFFFFF),
    tertiaryContainer  = GreenContainer,
    onTertiaryContainer = OnGreenContainer,
    background         = BackgroundLight,
    onBackground       = OnSurfaceLight,
    surface            = SurfaceLight,
    onSurface          = OnSurfaceLight,
    surfaceVariant     = SurfaceVariantLight,
    onSurfaceVariant   = OnSurfaceVariantLight,
    outline            = OutlineLight,
    error              = Color(0xFFB00020),
    onError            = Color(0xFFFFFFFF),
    errorContainer     = Color(0xFFFFDAD6),
    onErrorContainer   = Color(0xFF410002)
)

/**
 * Material 3 Color Scheme — Dark Mode
 * Warna lebih redup dan hangat untuk kenyamanan mata di malam hari.
 */
private val DarkColorScheme = darkColorScheme(
    primary            = OrangePrimaryLight,
    onPrimary          = Color(0xFF3E1500),
    primaryContainer   = Color(0xFF7B2900),
    onPrimaryContainer = Color(0xFFFFDBCA),
    secondary          = Color(0xFF90CAF9),
    onSecondary        = Color(0xFF003258),
    secondaryContainer = Color(0xFF004880),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary           = Color(0xFF81C784),
    onTertiary         = Color(0xFF003909),
    tertiaryContainer  = Color(0xFF005312),
    onTertiaryContainer = Color(0xFF97F4A0),
    background         = BackgroundDark,
    onBackground       = OnSurfaceDark,
    surface            = SurfaceDark,
    onSurface          = OnSurfaceDark,
    surfaceVariant     = SurfaceVariantDark,
    onSurfaceVariant   = OnSurfaceVariantDark,
    outline            = OutlineDark,
    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF690005),
    errorContainer     = Color(0xFF93000A),
    onErrorContainer   = Color(0xFFFFDAD6)
)

/**
 * GempaIDTheme — Root composable theme untuk seluruh aplikasi.
 *
 * @param darkTheme: dikontrol dari ViewModel via DataStore (user preference),
 *                   bukan bergantung pada system setting saja.
 * @param dynamicColor: Material You dynamic color (Android 12+).
 *                      Dinonaktifkan agar warna app konsisten di semua device.
 * @param content: semua UI yang dibungkus tema ini.
 *
 * SideEffect: mengatur warna status bar agar sesuai dengan tema aktif.
 * WindowCompat: memastikan konten tidak tertutup status bar (edge-to-edge).
 */
@Composable
fun GempaIDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic Color (Material You) — Android 12+ hanya jika diaktifkan
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Mengatur warna status bar agar transparan (edge-to-edge)
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GempaTypography,
        content     = content
    )
}
