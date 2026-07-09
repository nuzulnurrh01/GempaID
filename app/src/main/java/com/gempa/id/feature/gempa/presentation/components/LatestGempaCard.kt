package com.gempa.id.feature.gempa.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gempa.id.feature.gempa.domain.model.Gempa

/**
 * LatestGempaCard — Hero card yang menampilkan gempa terbaru.
 *
 * Desain: gradient background berdasarkan tingkat keparahan gempa,
 * dengan animasi "pulse" pada badge magnitudo untuk menarik perhatian.
 *
 * Ini adalah elemen UI paling dominan di HomeScreen — harus langsung
 * mengkomunikasikan informasi paling kritis: magnitudo, lokasi, dan
 * apakah berpotensi tsunami.
 *
 * @param gempa data gempa terbaru
 * @param onClick callback navigasi ke detail
 */
@Composable
fun LatestGempaCard(
    gempa: Gempa,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Warna gradient berdasarkan kategori magnitudo
    val baseColor = magnitudeCategoryColor(gempa.magnitudeCategory)
    val gradientColors = listOf(
        baseColor.copy(alpha = 0.9f),
        baseColor.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    // Animasi pulse untuk badge magnitudo besar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (gempa.magnitude >= 5.0) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // ── Header row ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Label "GEMPA TERKINI"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "GEMPA TERKINI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )
                    }

                    TsunamiChip(hasTsunamiPotential = gempa.hasTsunamiPotential)
                }

                Spacer(Modifier.height(16.dp))

                // ── Main content ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Magnitude badge dengan animasi pulse
                    MagnitudeBadge(
                        magnitude = gempa.magnitude,
                        category  = gempa.magnitudeCategory,
                        size      = BadgeSize.LARGE,
                        modifier  = Modifier.scale(scale)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = gempa.wilayah,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${gempa.magnitudeCategory.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = magnitudeCategoryColor(gempa.magnitudeCategory),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Info row: waktu & koordinat ────────────────────────────
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip(
                        icon  = Icons.Filled.AccessTime,
                        label = "${gempa.tanggal}\n${gempa.jam}"
                    )
                    InfoChip(
                        icon  = Icons.Filled.Layers,
                        label = "Kedalaman\n${gempa.kedalaman}"
                    )
                    InfoChip(
                        icon  = Icons.Filled.LocationOn,
                        label = "${gempa.lintang}\n${gempa.bujur}"
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Potensi (jika ada) ──────────────────────────────────────
                if (gempa.dirasakan.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Vibration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Dirasakan: ${gempa.dirasakan}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Tap for detail hint
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lihat detail",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * InfoChip — chip kecil untuk menampilkan satu item informasi di LatestGempaCard.
 */
@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
