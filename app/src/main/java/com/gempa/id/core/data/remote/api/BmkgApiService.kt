package com.gempa.id.core.data.remote.api

import com.gempa.id.core.data.remote.dto.AutoGempaResponse
import com.gempa.id.core.data.remote.dto.GempaTerkiniResponse
import retrofit2.http.GET

/**
 * BmkgApiService — Interface Retrofit untuk BMKG Open Data API.
 *
 * BMKG menyediakan data gempa secara publik tanpa autentikasi.
 * Base URL: https://data.bmkg.go.id/DataMKG/TEWS/
 *
 * Retrofit menggunakan interface ini untuk men-generate implementasi HTTP
 * secara otomatis pada runtime — ini adalah pola Repository/Service layer
 * yang memisahkan concern jaringan dari business logic.
 *
 * Semua fungsi adalah suspend function → berjalan di Coroutine,
 * tidak memblokir Main Thread.
 */
interface BmkgApiService {

    /**
     * Mengambil data gempa terbaru (1 kejadian terakhir).
     * Endpoint: autogempa.json
     * Digunakan untuk tampilan "Gempa Terkini" di halaman utama.
     */
    @GET("autogempa.json")
    suspend fun getAutoGempa(): AutoGempaResponse

    /**
     * Mengambil 15 gempa terkini.
     * Endpoint: gempaterkini.json
     * Digunakan untuk daftar riwayat gempa (LazyColumn).
     */
    @GET("gempaterkini.json")
    suspend fun getGempaTerkini(): GempaTerkiniResponse
}
