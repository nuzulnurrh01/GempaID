package com.gempa.id.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NetworkMonitor — Utility class untuk monitoring status koneksi internet.
 *
 * Menggunakan ConnectivityManager.NetworkCallback (API 26+) yang lebih
 * akurat dan efisien dibanding BroadcastReceiver lama.
 *
 * callbackFlow: mengkonversi callback-based API ke Kotlin Flow,
 * memungkinkan UI untuk bereaksi secara reaktif terhadap perubahan koneksi.
 *
 * Digunakan di ViewModel untuk:
 * - Menampilkan banner "Offline Mode" saat tidak ada internet
 * - Menentukan apakah perlu coba fetch dari API atau tampilkan cache
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Flow yang mengamati status koneksi internet secara real-time.
     * Emit: true = ada internet, false = tidak ada internet.
     * distinctUntilChanged() mencegah duplicate emission status yang sama.
     */
    val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Callback yang dipanggil oleh sistem saat status jaringan berubah
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("Network available")
                trySend(true)  // trySend aman dari blocking/exception
            }

            override fun onLost(network: Network) {
                Timber.d("Network lost")
                trySend(false)
            }

            override fun onUnavailable() {
                Timber.d("Network unavailable")
                trySend(false)
            }
        }

        // Request untuk memantau jaringan dengan kemampuan internet
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Emit status saat ini saat Flow mulai dikumpulkan
        trySend(isCurrentlyConnected(connectivityManager))

        // awaitClose: unregister callback saat Flow di-cancel (lifecycle-aware)
        awaitClose {
            Timber.d("Unregistering network callback")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * Cek status koneksi saat ini (synchronous, untuk initial check).
     */
    fun isCurrentlyConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return isCurrentlyConnected(cm)
    }

    private fun isCurrentlyConnected(cm: ConnectivityManager): Boolean {
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
