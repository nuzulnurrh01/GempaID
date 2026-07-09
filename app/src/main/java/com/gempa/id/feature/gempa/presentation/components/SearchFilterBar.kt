package com.gempa.id.feature.gempa.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * SearchFilterBar — Komponen search + filter magnitude untuk HomeScreen.
 *
 * Terdiri dari:
 * 1. TextField untuk pencarian wilayah (real-time, debounced di ViewModel)
 * 2. FilterChips untuk filter cepat berdasarkan magnitude
 *
 * State dikelola di ViewModel (hoisted state pattern):
 * - searchQuery dari ViewModel
 * - onQueryChange callback ke ViewModel
 *
 * Prinsip "State Hoisting": composable ini stateless,
 * semua state dikelola oleh parent/ViewModel.
 *
 * @param searchQuery nilai query saat ini
 * @param onQueryChange dipanggil saat user mengetik
 * @param selectedMagnitude filter magnitude yang aktif (0.0 = semua)
 * @param onMagnitudeSelected dipanggil saat filter dipilih
 * @param onClearFilters dipanggil saat tombol hapus filter diklik
 */
@Composable
fun SearchFilterBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedMagnitude: Double,
    onMagnitudeSelected: (Double) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Search TextField ──────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Cari wilayah gempa...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Cari",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                // Tampilkan tombol clear hanya jika ada teks
                AnimatedVisibility(
                    visible = searchQuery.isNotBlank(),
                    enter = scaleIn() + fadeIn(),
                    exit  = scaleOut() + fadeOut()
                ) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Hapus pencarian",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        // ── Magnitude Filter Chips ────────────────────────────────────────
        val magnitudeOptions = listOf(
            0.0  to "Semua",
            3.0  to "M3+",
            4.0  to "M4+",
            5.0  to "M5+",
            6.0  to "M6+"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            magnitudeOptions.forEach { (value, label) ->
                FilterChip(
                    selected = selectedMagnitude == value,
                    onClick  = { onMagnitudeSelected(value) },
                    label    = {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // Tombol hapus semua filter — muncul jika ada filter aktif
            AnimatedVisibility(
                visible = searchQuery.isNotBlank() || selectedMagnitude > 0.0,
                enter   = scaleIn() + fadeIn(),
                exit    = scaleOut() + fadeOut()
            ) {
                IconButton(
                    onClick = onClearFilters,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterAltOff,
                        contentDescription = "Hapus semua filter",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
