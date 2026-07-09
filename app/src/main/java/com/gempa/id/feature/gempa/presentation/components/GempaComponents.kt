package com.gempa.id.feature.gempa.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gempa.id.feature.gempa.domain.model.Gempa
import com.gempa.id.feature.gempa.domain.model.MagnitudeCategory
import com.gempa.id.ui.theme.*

/**
 * Komponen UI yang dapat digunakan ulang di seluruh aplikasi.
 *
 * Prinsip Compose: pisahkan UI menjadi komponen kecil yang:
 * 1. Stateless (menerima data sebagai parameter)
 * 2. Dapat dipreview secara independen
 * 3. Mudah di-test
 * 4. Dapat dikomposisikan (composable)
 */

// ── Magnitude Badge ───────────────────────────────────────────────────────────

/**
 * MagnitudeBadge — Badge visual yang menampilkan nilai magnitudo dengan warna
 * yang sesuai kategori kekuatan gempa.
 *
 * @param magnitude nilai magnitudo (misal: 5.2)
 * @param category kategori dari domain model (LIGHT, MODERATE, dll)
 * @param size ukuran badge (SMALL, MEDIUM, LARGE)
 */
@Composable
fun MagnitudeBadge(
    magnitude: Double,
    category: MagnitudeCategory,
    size: BadgeSize = BadgeSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    val badgeColor = magnitudeCategoryColor(category)
    val textColor = if (category.ordinal >= MagnitudeCategory.MODERATE.ordinal) {
        Color.White
    } else {
        Color(0xFF1C1917)
    }

    val (badgeDp, fontSize) = when (size) {
        BadgeSize.SMALL  -> 40.dp to 14.sp
        BadgeSize.MEDIUM -> 56.dp to 18.sp
        BadgeSize.LARGE  -> 80.dp to 26.sp
    }

    Box(
        modifier = modifier
            .size(badgeDp)
            .clip(CircleShape)
            .background(badgeColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = String.format("%.1f", magnitude),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
    }
}

enum class BadgeSize { SMALL, MEDIUM, LARGE }

/**
 * Mapping MagnitudeCategory ke Color.
 * Dipisah sebagai function agar bisa digunakan di tempat lain.
 */
fun magnitudeCategoryColor(category: MagnitudeCategory): Color = when (category) {
    MagnitudeCategory.MICRO    -> MagnitudeMicro
    MagnitudeCategory.MINOR    -> MagnitudeMinor
    MagnitudeCategory.LIGHT    -> MagnitudeLight
    MagnitudeCategory.MODERATE -> MagnitudeModerate
    MagnitudeCategory.STRONG   -> MagnitudeStrong
    MagnitudeCategory.MAJOR    -> MagnitudeMajor
    MagnitudeCategory.GREAT    -> MagnitudeGreat
}

// ── Tsunami Status Chip ───────────────────────────────────────────────────────

/**
 * TsunamiChip — Chip yang menampilkan status potensi tsunami.
 * Merah = berpotensi tsunami, Hijau = tidak berpotensi.
 */
@Composable
fun TsunamiChip(
    hasTsunamiPotential: Boolean,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, icon, label) = if (hasTsunamiPotential) {
        arrayOf(TsunamiWarningLight, TsunamiWarning, Icons.Filled.Warning, "Berpotensi Tsunami")
    } else {
        arrayOf(TsunamiSafeLight, TsunamiSafe, Icons.Filled.CheckCircle, "Tidak Berpotensi Tsunami")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor as Color,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                contentDescription = null,
                tint = contentColor as Color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label as String,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Offline Banner ────────────────────────────────────────────────────────────

/**
 * OfflineBanner — Banner animasi yang muncul di atas layar saat tidak ada internet.
 * Menggunakan AnimatedVisibility agar transisi masuk/keluar halus.
 */
@Composable
fun OfflineBanner(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Mode Offline — Menampilkan data terakhir",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// ── Shimmer Loading ───────────────────────────────────────────────────────────

/**
 * ShimmerEffect — efek loading berkilau (skeleton screen).
 * Lebih baik dari spinner karena memberi gambaran layout konten.
 *
 * Implementasi: animated gradient yang bergeser dari kiri ke kanan.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 200f, 0f),
        end   = Offset(translateAnimation, 0f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * GempaCardShimmer — Skeleton loader untuk GempaListItem.
 * Ditampilkan saat data sedang dimuat pertama kali.
 */
@Composable
fun GempaCardShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(modifier = Modifier.size(56.dp), shape = CircleShape)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp))
            }
        }
    }
}

/**
 * Shimmer list untuk kondisi loading — menampilkan 5 card placeholder.
 */
@Composable
fun ShimmerLoadingList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(5) {
            GempaCardShimmer()
        }
    }
}

// ── Error State Component ─────────────────────────────────────────────────────

/**
 * ErrorStateView — Tampilan error yang informatif dengan tombol retry.
 * Mengikuti prinsip: "Errors don't apologize, and are never vague."
 */
@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Gagal Memuat Data",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Coba Lagi")
        }
    }
}

// ── Empty State Component ─────────────────────────────────────────────────────

/**
 * EmptyStateView — Tampilan ketika tidak ada data yang cocok dengan filter/search.
 */
@Composable
fun EmptyStateView(
    title: String = "Tidak Ada Data",
    subtitle: String = "Tidak ditemukan gempa yang sesuai pencarian.",
    onClearFilter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (onClearFilter != null) {
            OutlinedButton(onClick = onClearFilter) {
                Text("Hapus Filter")
            }
        }
    }
}

// ── Info Row (untuk Detail Screen) ───────────────────────────────────────────

/**
 * InfoRow — Baris informasi dengan label dan nilai.
 * Digunakan di DetailScreen untuk menampilkan data terstruktur.
 */
@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .offset(y = 2.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
