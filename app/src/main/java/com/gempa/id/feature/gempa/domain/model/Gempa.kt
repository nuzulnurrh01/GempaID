package com.gempa.id.feature.gempa.domain.model

/**
 * Gempa — Domain Model (Pure Kotlin, tanpa dependency Android/Room/Retrofit).
 *
 * Ini adalah "bahasa" yang digunakan di Business Logic dan UI.
 * Layer UI TIDAK boleh mengenal GempaDto (network) atau GempaEntity (database).
 *
 * Keuntungan pemisahan ini:
 * 1. Mudah di-test tanpa mock database/network
 * 2. Perubahan struktur API/database tidak merembet ke UI
 * 3. Tipe data sudah bersih (magnitude sebagai Double, bukan String)
 *
 * Pola ini disebut "Clean Architecture" — data mengalir dari luar ke dalam:
 * API → DTO → Mapper → Domain Model → UI
 */
data class Gempa(
    val dateTime: String,
    val tanggal: String,
    val jam: String,
    val coordinates: String,
    val lintang: String,
    val bujur: String,
    val magnitude: Double,
    val kedalaman: String,
    val wilayah: String,
    val potensi: String,
    val dirasakan: String,
    val shakemapUrl: String,
    val isLatest: Boolean = false
) {
    /**
     * Computed property: kategori magnitudo berdasarkan skala Richter.
     * Berguna untuk menentukan warna/ikon di UI tanpa logika di Composable.
     */
    val magnitudeCategory: MagnitudeCategory
        get() = when {
            magnitude < 3.0 -> MagnitudeCategory.MICRO
            magnitude < 4.0 -> MagnitudeCategory.MINOR
            magnitude < 5.0 -> MagnitudeCategory.LIGHT
            magnitude < 6.0 -> MagnitudeCategory.MODERATE
            magnitude < 7.0 -> MagnitudeCategory.STRONG
            magnitude < 8.0 -> MagnitudeCategory.MAJOR
            else            -> MagnitudeCategory.GREAT
        }

    /**
     * Computed property: apakah gempa berpotensi tsunami.
     * BMKG menuliskan "Tidak berpotensi tsunami" jika aman.
     */
    val hasTsunamiPotential: Boolean
        get() = !potensi.contains("Tidak", ignoreCase = true)

    /**
     * Parsed koordinat latitude untuk peta.
     */
    val latitude: Double
        get() = coordinates.split(",").firstOrNull()?.trim()?.toDoubleOrNull() ?: 0.0

    /**
     * Parsed koordinat longitude untuk peta.
     */
    val longitude: Double
        get() = coordinates.split(",").lastOrNull()?.trim()?.toDoubleOrNull() ?: 0.0
}

/**
 * MagnitudeCategory — Enum untuk kategorisasi kekuatan gempa.
 * Digunakan di UI untuk pewarnaan dan label yang informatif.
 */
enum class MagnitudeCategory(val label: String, val colorHex: String) {
    MICRO("Mikro < 3.0", "#4CAF50"),
    MINOR("Minor 3.0–3.9", "#8BC34A"),
    LIGHT("Lemah 4.0–4.9", "#FFC107"),
    MODERATE("Sedang 5.0–5.9", "#FF9800"),
    STRONG("Kuat 6.0–6.9", "#FF5722"),
    MAJOR("Mayor 7.0–7.9", "#F44336"),
    GREAT("Dahsyat 8.0+", "#B71C1C")
}
