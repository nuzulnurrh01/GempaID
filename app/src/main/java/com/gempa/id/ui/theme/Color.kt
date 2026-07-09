package com.gempa.id.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color Palette — Sistem warna GempaID.
 *
 * Konsep desain: "Seismic Dashboard"
 * Warna terinspirasi dari tampilan seismograf dan sistem peringatan dini:
 * - Primary: Oranye Peringatan (energi, urgensi, tapi tidak menakutkan seperti merah)
 * - Secondary: Biru Tenang (data, kepercayaan, ilmiah)
 * - Tertiary: Hijau Aman (status aman, tidak berpotensi tsunami)
 * - Error/Danger: Merah untuk gempa besar / tsunami potential
 *
 * Magnitude Color Scale (dari aman ke berbahaya):
 * Micro → Minor → Light → Moderate → Strong → Major → Great
 */

// ── Primary — Oranye Seismik ─────────────────────────────────────────────────
val OrangePrimary      = Color(0xFFE65100)   // Deep Orange 900 — identitas utama
val OrangePrimaryLight = Color(0xFFFF6D00)   // Deep Orange A700
val OrangeContainer    = Color(0xFFFFE0B2)   // Orange 100 — container light
val OnOrangeContainer  = Color(0xFF3E2723)   // Dark Brown on container

// ── Secondary — Biru Ilmiah ──────────────────────────────────────────────────
val BlueSecondary      = Color(0xFF0D47A1)   // Blue 900
val BlueSecondaryLight = Color(0xFF1565C0)   // Blue 800
val BlueContainer      = Color(0xFFBBDEFB)   // Blue 100
val OnBlueContainer    = Color(0xFF0D1B2A)

// ── Tertiary — Hijau Aman ────────────────────────────────────────────────────
val GreenTertiary      = Color(0xFF1B5E20)   // Green 900
val GreenContainer     = Color(0xFFC8E6C9)   // Green 100
val OnGreenContainer   = Color(0xFF1B2E1B)

// ── Neutral Surface ───────────────────────────────────────────────────────────
val SurfaceLight       = Color(0xFFFFFBF0)   // Warm White — sedikit cream, tidak dingin
val SurfaceDark        = Color(0xFF1A1410)   // Very Dark Brown — hangat di malam hari
val SurfaceVariantLight= Color(0xFFF3EDE0)
val SurfaceVariantDark = Color(0xFF2C2420)
val BackgroundLight    = Color(0xFFFFF8F0)
val BackgroundDark     = Color(0xFF120E0A)

// ── On-Colors ────────────────────────────────────────────────────────────────
val OnSurfaceLight     = Color(0xFF1C1917)
val OnSurfaceDark      = Color(0xFFEDE0D4)
val OnSurfaceVariantLight = Color(0xFF5D4037)
val OnSurfaceVariantDark  = Color(0xFFBCAAA4)
val OutlineLight       = Color(0xFF8D6E63)
val OutlineDark        = Color(0xFF6D4C41)

// ── Magnitude Severity Colors — sistem warna untuk kekuatan gempa ────────────
val MagnitudeMicro     = Color(0xFF4CAF50)   // Hijau — < 3.0
val MagnitudeMinor     = Color(0xFF8BC34A)   // Hijau Muda — 3.0-3.9
val MagnitudeLight     = Color(0xFFFFEB3B)   // Kuning — 4.0-4.9
val MagnitudeModerate  = Color(0xFFFF9800)   // Oranye — 5.0-5.9
val MagnitudeStrong    = Color(0xFFFF5722)   // Deep Orange — 6.0-6.9
val MagnitudeMajor     = Color(0xFFF44336)   // Merah — 7.0-7.9
val MagnitudeGreat     = Color(0xFF7B1FA2)   // Ungu Gelap — 8.0+

// ── Tsunami Warning ───────────────────────────────────────────────────────────
val TsunamiWarning     = Color(0xFFB71C1C)   // Deep Red
val TsunamiSafe        = Color(0xFF2E7D32)   // Dark Green
val TsunamiWarningLight= Color(0xFFFFCDD2)   // Red 100 container
val TsunamiSafeLight   = Color(0xFFC8E6C9)   // Green 100 container
