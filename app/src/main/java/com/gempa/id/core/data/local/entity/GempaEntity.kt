package com.gempa.id.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * GempaEntity — Representasi tabel "gempa_table" di Room Database.
 *
 * @Entity: Room akan membuat tabel berdasarkan class ini pada compile time.
 * @PrimaryKey: dateTime digunakan sebagai ID unik karena setiap gempa
 * memiliki timestamp ISO 8601 yang unik dari BMKG.
 *
 * Pola caching: data dari API disimpan ke Room, UI selalu membaca dari Room.
 * Ini memastikan aplikasi tetap bisa digunakan saat offline (offline-first architecture).
 *
 * cachedAt: timestamp kapan data ini di-cache, digunakan untuk
 * menentukan apakah perlu refresh dari API (cache invalidation strategy).
 */
@Entity(tableName = "gempa_table")
data class GempaEntity(
    @PrimaryKey
    val dateTime: String,       // ISO 8601 — "2025-01-10T14:30:00+07:00"
    val tanggal: String,        // "10 Jan 2025"
    val jam: String,            // "14:30:00 WIB"
    val coordinates: String,    // "-8.50,115.20"
    val lintang: String,        // "-8.50 LS"
    val bujur: String,          // "115.20 BT"
    val magnitude: Double,      // 5.2 (sudah dikonversi dari String)
    val kedalaman: String,      // "10 km"
    val wilayah: String,        // "Bali"
    val potensi: String,        // "Tidak berpotensi tsunami"
    val dirasakan: String,      // "III MMI di Denpasar"
    val shakemap: String,       // nama file shakemap
    val isLatest: Boolean = false,  // flag untuk gempa terbaru (autogempa)
    val cachedAt: Long = System.currentTimeMillis()  // epoch ms
)
