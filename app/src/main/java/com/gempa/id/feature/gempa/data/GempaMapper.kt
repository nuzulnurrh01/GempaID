package com.gempa.id.feature.gempa.data

import com.gempa.id.core.data.local.entity.GempaEntity
import com.gempa.id.core.data.remote.dto.GempaDto
import com.gempa.id.feature.gempa.domain.model.Gempa

/**
 * GempaMapper — Fungsi mapper untuk konversi antar layer data.
 *
 * Menggunakan top-level extension functions agar mudah diakses di seluruh aplikasi
 * dan kompatibel dengan build tools (KSP/Hilt).
 */

private const val BMKG_SHAKEMAP_BASE_URL = "https://data.bmkg.go.id/DataMKG/TEWS/"

// ── DTO → Entity ─────────────────────────────────────────────────────────

fun GempaDto.toEntity(isLatest: Boolean = false): GempaEntity = GempaEntity(
    dateTime   = dateTime,
    tanggal    = tanggal,
    jam        = jam,
    coordinates = coordinates,
    lintang    = lintang,
    bujur      = bujur,
    magnitude  = magnitude.toDoubleOrNull() ?: 0.0,
    kedalaman  = kedalaman,
    wilayah    = wilayah,
    potensi    = potensi,
    dirasakan  = dirasakan,
    shakemap   = shakemap,
    isLatest   = isLatest,
    cachedAt   = System.currentTimeMillis()
)

// ── Entity → Domain Model ────────────────────────────────────────────────

fun GempaEntity.toDomain(): Gempa = Gempa(
    dateTime     = dateTime,
    tanggal      = tanggal,
    jam          = jam,
    coordinates  = coordinates,
    lintang      = lintang,
    bujur        = bujur,
    magnitude    = magnitude,
    kedalaman    = kedalaman,
    wilayah      = wilayah,
    potensi      = potensi,
    dirasakan    = dirasakan,
    shakemapUrl  = if (shakemap.isNotEmpty()) "$BMKG_SHAKEMAP_BASE_URL$shakemap" else "",
    isLatest     = isLatest
)

// ── DTO → Domain Model ───────────────────────────────────────────────────

fun GempaDto.toDomain(isLatest: Boolean = false): Gempa = Gempa(
    dateTime     = dateTime,
    tanggal      = tanggal,
    jam          = jam,
    coordinates  = coordinates,
    lintang      = lintang,
    bujur        = bujur,
    magnitude    = magnitude.toDoubleOrNull() ?: 0.0,
    kedalaman    = kedalaman,
    wilayah      = wilayah,
    potensi      = potensi,
    dirasakan    = dirasakan,
    shakemapUrl  = if (shakemap.isNotEmpty()) "$BMKG_SHAKEMAP_BASE_URL$shakemap" else "",
    isLatest     = isLatest
)

// ── List helpers ─────────────────────────────────────────────────────────

fun List<GempaDto>.toEntityList(): List<GempaEntity> = map { it.toEntity() }
fun List<GempaEntity>.toDomainList(): List<Gempa> = map { it.toDomain() }
