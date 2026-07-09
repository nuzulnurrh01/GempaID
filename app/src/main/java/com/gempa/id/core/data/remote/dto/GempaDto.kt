package com.gempa.id.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO (Data Transfer Object) — representasi 1:1 dari struktur JSON BMKG API.
 *
 * BMKG menyediakan dua endpoint utama:
 * 1. autogempa.json  → gempa terbaru (1 event)
 * 2. gempaterkini.json → 15 gempa terkini
 *
 * @Serializable: KotlinX Serialization akan men-generate parser pada compile time
 * (zero reflection, performa lebih cepat dari Gson/Moshi).
 *
 * @SerialName: mapping antara nama field JSON ("Infogempa") ke nama Kotlin yang idiomatik.
 */

// ── Response wrapper untuk autogempa.json ───────────────────────────────
@Serializable
data class AutoGempaResponse(
    @SerialName("Infogempa") val infoGempa: InfoGempa
)

// ── Response wrapper untuk gempaterkini.json ────────────────────────────
@Serializable
data class GempaTerkiniResponse(
    @SerialName("Infogempa") val infoGempa: InfoGempaTerkini
)

@Serializable
data class InfoGempa(
    @SerialName("gempa") val gempa: GempaDto
)

@Serializable
data class InfoGempaTerkini(
    @SerialName("gempa") val gempa: List<GempaDto>
)

/**
 * GempaDto — model data utama gempa dari BMKG.
 *
 * Catatan: semua field dari API adalah String karena BMKG tidak menggunakan
 * tipe numerik. Konversi ke Double/Int dilakukan di layer mapper (domain).
 */
@Serializable
data class GempaDto(
    @SerialName("Tanggal")   val tanggal: String   = "",   // "10 Jan 2025"
    @SerialName("Jam")       val jam: String       = "",   // "14:30:00 WIB"
    @SerialName("DateTime")  val dateTime: String  = "",   // ISO 8601 format
    @SerialName("Coordinates") val coordinates: String = "", // "-8.5,115.2"
    @SerialName("Lintang")   val lintang: String   = "",   // "-8.50 LS"
    @SerialName("Bujur")     val bujur: String     = "",   // "115.20 BT"
    @SerialName("Magnitude") val magnitude: String = "",   // "5.2"
    @SerialName("Kedalaman") val kedalaman: String = "",   // "10 km"
    @SerialName("Wilayah")   val wilayah: String   = "",   // "Bali"
    @SerialName("Potensi")   val potensi: String   = "",   // "Tidak berpotensi tsunami"
    @SerialName("Dirasakan") val dirasakan: String = "",   // "III MMI di Denpasar"
    @SerialName("Shakemap")  val shakemap: String  = ""    // "20250110143000.mmi.jpg"
)
