package com.gempa.id.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.gempa.id.feature.detail.presentation.screen.DetailScreen
import com.gempa.id.feature.gempa.presentation.screen.HomeScreen
import kotlinx.serialization.Serializable

/**
 * Route definitions menggunakan type-safe navigation (Navigation 2.8+).
 *
 * Type-safe navigation adalah fitur baru yang menggantikan string-based routing.
 * Keuntungan:
 * - Tidak ada typo — compiler yang catch error
 * - Parameter otomatis ter-serialize/deserialize
 * - IDE autocomplete support
 *
 * @Serializable dibutuhkan agar KotlinX Serialization bisa mengkonversi
 * route object ke/dari string untuk NavBackStack.
 */
sealed interface Screen {

    @Serializable
    data object Home : Screen

    @Serializable
    data class Detail(
        val dateTime: String,
        val wilayah: String,
        val magnitude: Double,
        val tanggal: String,
        val jam: String,
        val lintang: String,
        val bujur: String,
        val kedalaman: String,
        val potensi: String,
        val dirasakan: String,
        val shakemapUrl: String,
        val coordinates: String
    ) : Screen
}

/**
 * GempaNavHost — Root navigation host untuk seluruh aplikasi.
 *
 * enterTransition/exitTransition: animasi slide yang konsisten di semua route.
 * Animasi dipilih agar terasa natural mengikuti arah navigasi:
 * - Push (ke detail): slide dari kanan
 * - Pop (kembali): slide ke kanan
 */
@Composable
fun GempaNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        }
    ) {
        composable<Screen.Home> {
            HomeScreen(
                onNavigateToDetail = { gempa ->
                    // Navigasi ke detail dengan semua parameter gempa
                    navController.navigate(
                        Screen.Detail(
                            dateTime    = gempa.dateTime,
                            wilayah     = gempa.wilayah,
                            magnitude   = gempa.magnitude,
                            tanggal     = gempa.tanggal,
                            jam         = gempa.jam,
                            lintang     = gempa.lintang,
                            bujur       = gempa.bujur,
                            kedalaman   = gempa.kedalaman,
                            potensi     = gempa.potensi,
                            dirasakan   = gempa.dirasakan,
                            shakemapUrl = gempa.shakemapUrl,
                            coordinates = gempa.coordinates
                        )
                    )
                }
            )
        }

        composable<Screen.Detail> { backStackEntry ->
            // Type-safe extraction parameter dari NavBackStack
            val route = backStackEntry.toRoute<Screen.Detail>()
            DetailScreen(
                route = route,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
