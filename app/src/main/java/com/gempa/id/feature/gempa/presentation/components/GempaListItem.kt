package com.gempa.id.feature.gempa.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gempa.id.feature.gempa.domain.model.Gempa

/**
 * GempaListItem — Card komponen untuk satu item gempa di LazyColumn.
 *
 * Desain mengikuti Material 3 Card pattern:
 * - Elevated card dengan shadow subtle
 * - Leading element: MagnitudeBadge (info paling penting — magnitudo)
 * - Middle: nama wilayah, tanggal, kedalaman
 * - Trailing: TsunamiChip status
 *
 * Interaktif: seluruh card dapat diklik untuk navigasi ke detail.
 *
 * animateColorAsState: warna border beranimasi smooth saat
 * gempa baru muncul (highlight gempa terkini).
 *
 * @param gempa data domain model gempa
 * @param onClick callback saat card diklik
 * @param modifier Modifier untuk kustomisasi dari parent
 */
@Composable
fun GempaListItem(
    gempa: Gempa,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Warna border animasi — oranye untuk gempa terkini, transparan untuk lainnya
    val borderColor by animateColorAsState(
        targetValue = if (gempa.isLatest) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(600),
        label = "border_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gempa.isLatest) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (gempa.isLatest) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Leading: Magnitude Badge ───────────────────────────────────
            MagnitudeBadge(
                magnitude = gempa.magnitude,
                category  = gempa.magnitudeCategory,
                size      = BadgeSize.MEDIUM
            )

            // ── Middle: Informasi Wilayah & Waktu ─────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Label "TERKINI" jika ini gempa terbaru
                if (gempa.isLatest) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "● TERKINI",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }

                // Nama wilayah — info paling penting
                Text(
                    text = gempa.wilayah,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Tanggal dan jam
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${gempa.tanggal} · ${gempa.jam}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Koordinat dan kedalaman
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = gempa.coordinates,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = gempa.kedalaman,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Trailing: Tsunami Status & Arrow ──────────────────────────
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TsunamiChip(hasTsunamiPotential = gempa.hasTsunamiPotential)
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Lihat detail",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
