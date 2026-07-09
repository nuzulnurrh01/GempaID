package com.gempa.id.core.network

/**
 * UiState — Sealed class untuk merepresentasikan state UI secara type-safe.
 *
 * Pola ini menggantikan nullable variables dan boolean flags yang error-prone:
 * ❌ Bad:  var isLoading = true; var data = null; var error = null
 * ✅ Good: var state: UiState<T> = UiState.Loading
 *
 * Sealed class memastikan setiap state di-handle di `when` expression,
 * Kotlin compiler akan error jika ada branch yang terlewat.
 *
 * Generic type T memungkinkan reuse untuk tipe data apapun.
 */
sealed class UiState<out T> {

    /**
     * Loading: data sedang di-fetch dari network atau database.
     * UI menampilkan shimmer/skeleton atau progress indicator.
     */
    data object Loading : UiState<Nothing>()

    /**
     * Success: data berhasil di-fetch.
     * @param data hasil data yang siap ditampilkan di UI.
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error: terjadi kesalahan saat fetch data.
     * @param message pesan error yang human-readable untuk ditampilkan di UI.
     * @param throwable opsional, untuk logging/debugging.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()

    /**
     * Empty: request berhasil tapi data kosong.
     * Berbeda dari Error — ini kondisi yang valid (misalnya tidak ada gempa hari ini).
     */
    data object Empty : UiState<Nothing>()
}

/**
 * Extension function untuk transformasi data saat state Success.
 * Berguna di ViewModel untuk mengubah tipe data tanpa boilerplate.
 */
fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R> = when (this) {
    is UiState.Success -> UiState.Success(transform(data))
    is UiState.Loading -> UiState.Loading
    is UiState.Error   -> UiState.Error(message, throwable)
    is UiState.Empty   -> UiState.Empty
}
