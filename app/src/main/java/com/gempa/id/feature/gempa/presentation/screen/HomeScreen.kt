package com.gempa.id.feature.gempa.presentation.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gempa.id.core.network.UiState
import com.gempa.id.feature.gempa.domain.model.Gempa
import com.gempa.id.feature.gempa.presentation.components.*
import com.gempa.id.feature.gempa.presentation.viewmodel.GempaViewModel

/**
 * HomeScreen — Layar utama aplikasi GempaID.
 *
 * Struktur layar (dari atas ke bawah):
 * 1. TopAppBar: judul + tombol toggle tema
 * 2. OfflineBanner: muncul jika tidak ada internet
 * 3. LazyColumn (konten utama):
 *    - Header: LatestGempaCard (gempa terbaru sebagai hero section)
 *    - SearchFilterBar: pencarian dan filter
 *    - Daftar 15 gempa terkini
 *
 * PullToRefreshBox: user bisa tarik layar ke bawah untuk refresh data.
 * nestedScrollConnection: memungkinkan scroll behavior yang terkoordinasi
 * antara TopAppBar dan LazyColumn (TopAppBar ikut scroll).
 *
 * State management:
 * - collectAsStateWithLifecycle(): collect Flow secara lifecycle-aware
 *   (berhenti saat app di-background, lanjut saat aktif kembali)
 *
 * @param onNavigateToDetail callback navigasi ke DetailScreen
 * @param viewModel disuntikkan otomatis oleh Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDetail: (Gempa) -> Unit,
    viewModel: GempaViewModel = hiltViewModel()
) {
    // ── Collect state dari ViewModel ──────────────────────────────────────
    val latestGempaState  by viewModel.latestGempaState.collectAsStateWithLifecycle()
    val gempaTerkiniState by viewModel.gempaTerkiniState.collectAsStateWithLifecycle()
    val filteredGempaList by viewModel.filteredGempaList.collectAsStateWithLifecycle()
    val searchQuery       by viewModel.searchQuery.collectAsStateWithLifecycle()
    val magnitudeFilter   by viewModel.magnitudeFilter.collectAsStateWithLifecycle()
    val isOnline          by viewModel.isOnline.collectAsStateWithLifecycle()
    val isDarkMode        by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Loading state untuk pull-to-refresh indicator
    val isRefreshing = latestGempaState is UiState.Loading ||
                       gempaTerkiniState is UiState.Loading

    // Scroll behavior untuk kolaps TopAppBar saat scroll
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState  = rememberLazyListState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GempaID",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Monitor Gempa Indonesia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(28.dp)
                    )
                },
                actions = {
                    // Tombol toggle tema Dark/Light
                    IconButton(onClick = viewModel::toggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.LightMode
                                          else Icons.Filled.DarkMode,
                            contentDescription = if (isDarkMode) "Mode Terang"
                                                 else "Mode Gelap",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Tombol refresh manual
                    IconButton(onClick = viewModel::refreshAll) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh data",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor          = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor  = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->

        // Kolom utama — OfflineBanner di luar PullToRefresh agar selalu visible
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Offline Banner ────────────────────────────────────────────
            OfflineBanner(isVisible = !isOnline)

            // ── Pull to Refresh ───────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh    = viewModel::refreshAll,
                modifier     = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state  = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical   = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // ── Section 1: Hero — Gempa Terkini ──────────────────
                    item(key = "latest_header") {
                        Text(
                            text = "Gempa Terbaru",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    item(key = "latest_card") {
                        when (val state = latestGempaState) {
                            is UiState.Loading -> {
                                // Skeleton loader untuk hero card
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )
                            }
                            is UiState.Success -> {
                                LatestGempaCard(
                                    gempa   = state.data,
                                    onClick = { onNavigateToDetail(state.data) }
                                )
                            }
                            is UiState.Error -> {
                                ErrorStateView(
                                    message = state.message,
                                    onRetry = viewModel::fetchLatestGempa
                                )
                            }
                            is UiState.Empty -> {
                                EmptyStateView(
                                    title    = "Tidak Ada Data Terbaru",
                                    subtitle = "Data gempa terbaru tidak tersedia saat ini."
                                )
                            }
                        }
                    }

                    item(key = "divider_1") {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── Section 2: Search & Filter Bar ───────────────────
                    item(key = "search_bar") {
                        SearchFilterBar(
                            searchQuery         = searchQuery,
                            onQueryChange       = viewModel::onSearchQueryChange,
                            selectedMagnitude   = magnitudeFilter,
                            onMagnitudeSelected = viewModel::onMagnitudeFilterChange,
                            onClearFilters      = viewModel::clearFilters
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── Section 3: Daftar Gempa ───────────────────────────
                    item(key = "list_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "15 Gempa Terkini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            // Hitung jumlah gempa setelah filter
                            AnimatedContent(
                                targetState = filteredGempaList.size,
                                label = "count"
                            ) { count ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "$count gempa",
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp, vertical = 2.dp
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    // ── Conditional list content ──────────────────────────
                    when (val state = gempaTerkiniState) {
                        is UiState.Loading -> {
                            item(key = "shimmer_list") {
                                ShimmerLoadingList()
                            }
                        }
                        is UiState.Error -> {
                            item(key = "error_view") {
                                ErrorStateView(
                                    message = state.message,
                                    onRetry = viewModel::fetchGempaTerkini
                                )
                            }
                        }
                        is UiState.Empty, is UiState.Success -> {
                            if (filteredGempaList.isEmpty()) {
                                item(key = "empty_view") {
                                    EmptyStateView(
                                        title    = "Tidak Ada Hasil",
                                        subtitle = "Coba ubah kata kunci atau filter pencarian.",
                                        onClearFilter = viewModel::clearFilters
                                    )
                                }
                            } else {
                                // ── LazyColumn list items ─────────────────
                                // key = dateTime agar Compose dapat mengelola
                                // recomposition secara efisien (stable key)
                                items(
                                    items = filteredGempaList,
                                    key   = { gempa -> gempa.dateTime }
                                ) { gempa ->
                                    GempaListItem(
                                        gempa   = gempa,
                                        onClick = { onNavigateToDetail(gempa) },
                                        // animateItem(): smooth animation saat item masuk/keluar list
                                        modifier = Modifier.animateItem(
                                            fadeInSpec   = null,
                                            fadeOutSpec  = null
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Spacer di bawah agar content tidak tertutup system bar
                    item(key = "bottom_spacer") {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
