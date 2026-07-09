package com.gempa.id.feature.detail.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gempa.id.core.navigation.Screen
import com.gempa.id.feature.gempa.domain.model.Gempa
import com.gempa.id.feature.gempa.domain.model.MagnitudeCategory
import com.gempa.id.feature.gempa.presentation.components.*

/**
 * DetailScreen — Halaman detail satu kejadian gempa.
 *
 * Menerima data via Navigation route (type-safe params) — tidak perlu
 * query ulang ke database/API karena semua data sudah ada di navArgs.
 *
 * Konten:
 * 1. Hero: magnitude badge besar + nama wilayah + tsunami chip
 * 2. Info grid: tanggal, jam, koordinat, kedalaman, magnitudo
 * 3. Shakemap: gambar peta guncangan dari BMKG (loaded via Coil)
 * 4. Keterangan MMI: penjelasan skala MMI yang dirasakan
 * 5. Potensi: status tsunami dengan penjelasan
 *
 * Coil AsyncImage: loading gambar yang efisien dengan:
 * - Placeholder saat loading
 * - Error fallback jika gambar gagal dimuat
 * - Caching otomatis
 *
 * @param route data gempa dari navigation
 * @param onNavigateBack callback untuk kembali
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    route: Screen.Detail,
    onNavigateBack: () -> Unit
) {
    // Rekonstruksi domain model dari route parameter
    val gempa = rememberGempaFromRoute(route)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Gempa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Section 1: Hero ───────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Magnitude badge besar
                    MagnitudeBadge(
                        magnitude = gempa.magnitude,
                        category  = gempa.magnitudeCategory,
                        size      = BadgeSize.LARGE
                    )

                    // Kategori label
                    Text(
                        text = gempa.magnitudeCategory.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = magnitudeCategoryColor(gempa.magnitudeCategory),
                        fontWeight = FontWeight.Bold
                    )

                    // Nama wilayah
                    Text(
                        text = gempa.wilayah,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Tsunami status chip
                    TsunamiChip(hasTsunamiPotential = gempa.hasTsunamiPotential)
                }
            }

            // ── Section 2: Info Detail ────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Informasi Gempa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    InfoRow(
                        icon  = Icons.Filled.CalendarToday,
                        label = "Tanggal",
                        value = gempa.tanggal
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    InfoRow(
                        icon  = Icons.Filled.AccessTime,
                        label = "Waktu",
                        value = gempa.jam
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    InfoRow(
                        icon  = Icons.Filled.MyLocation,
                        label = "Lintang",
                        value = gempa.lintang
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    InfoRow(
                        icon  = Icons.Filled.Explore,
                        label = "Bujur",
                        value = gempa.bujur
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    InfoRow(
                        icon  = Icons.Filled.Layers,
                        label = "Kedalaman",
                        value = gempa.kedalaman
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    InfoRow(
                        icon  = Icons.Filled.Vibration,
                        label = "Magnitudo",
                        value = "M ${gempa.magnitude}"
                    )

                    // Dirasakan — ditampilkan hanya jika ada data
                    if (gempa.dirasakan.isNotBlank()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        InfoRow(
                            icon  = Icons.Filled.SensorOccupied,
                            label = "Dirasakan",
                            value = gempa.dirasakan
                        )
                    }
                }
            }

            // ── Section 3: Shakemap ───────────────────────────────────────
            if (gempa.shakemapUrl.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Peta Guncangan (Shakemap)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Coil AsyncImage untuk loading gambar shakemap dari BMKG
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(gempa.shakemapUrl)
                                .crossfade(true)  // fade-in animation saat gambar muncul
                                .build(),
                            contentDescription = "Peta guncangan gempa di ${gempa.wilayah}",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            // Placeholder saat loading
                            placeholder = coil.compose.rememberAsyncImagePainter(
                                model = android.R.drawable.ic_menu_gallery
                            ),
                            // Error fallback jika gambar gagal dimuat (Syarat Poin C5)
                            error = coil.compose.rememberAsyncImagePainter(
                                model = android.R.drawable.ic_dialog_alert
                            )
                        )

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Sumber: BMKG (Badan Meteorologi, Klimatologi, dan Geofisika)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Section 4: Potensi Tsunami ────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (gempa.hasTsunamiPotential) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (gempa.hasTsunamiPotential)
                            Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (gempa.hasTsunamiPotential)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Status Tsunami",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = gempa.potensi,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── Section 5: Disclaimer ────────────────────────────────────
            Text(
                text = "Data bersumber dari BMKG (Badan Meteorologi, Klimatologi, dan Geofisika) Indonesia. Informasi ini hanya untuk keperluan edukasi dan monitoring.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

/**
 * Helper function untuk merekonstruksi Gempa domain model dari navigation route.
 * remember agar tidak di-recreate setiap recomposition.
 */
@Composable
private fun rememberGempaFromRoute(route: Screen.Detail): Gempa {
    return remember(route) {
        val magnitudeVal = route.magnitude
        val category = when {
            magnitudeVal < 3.0 -> MagnitudeCategory.MICRO
            magnitudeVal < 4.0 -> MagnitudeCategory.MINOR
            magnitudeVal < 5.0 -> MagnitudeCategory.LIGHT
            magnitudeVal < 6.0 -> MagnitudeCategory.MODERATE
            magnitudeVal < 7.0 -> MagnitudeCategory.STRONG
            magnitudeVal < 8.0 -> MagnitudeCategory.MAJOR
            else               -> MagnitudeCategory.GREAT
        }

        Gempa(
            dateTime    = route.dateTime,
            tanggal     = route.tanggal,
            jam         = route.jam,
            coordinates = route.coordinates,
            lintang     = route.lintang,
            bujur       = route.bujur,
            magnitude   = route.magnitude,
            kedalaman   = route.kedalaman,
            wilayah     = route.wilayah,
            potensi     = route.potensi,
            dirasakan   = route.dirasakan,
            shakemapUrl = route.shakemapUrl
        )
    }
}
