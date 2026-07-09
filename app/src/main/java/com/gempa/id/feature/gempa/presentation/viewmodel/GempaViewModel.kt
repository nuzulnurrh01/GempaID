package com.gempa.id.feature.gempa.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gempa.id.core.data.local.datastore.ThemeDataStore
import com.gempa.id.core.data.repository.GempaRepository
import com.gempa.id.core.network.UiState
import com.gempa.id.core.util.NetworkMonitor
import com.gempa.id.feature.gempa.domain.model.Gempa
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * GempaViewModel — ViewModel utama yang mengelola state UI untuk halaman daftar gempa.
 *
 * Tanggung jawab ViewModel:
 * 1. Mengambil data dari Repository
 * 2. Mengekspos state ke UI melalui StateFlow
 * 3. Survive konfigurasi ulang (rotasi layar) — data tidak hilang
 * 4. Mengelola Coroutine dengan viewModelScope
 *
 * @HiltViewModel: memungkinkan Hilt menginjeksi dependency ke ViewModel.
 * Hilt menangani lifecycle ViewModel secara otomatis.
 *
 * TIDAK ada referensi ke Context, Activity, atau View — ViewModel harus
 * bersih dari Android framework untuk kemudahan testing.
 */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class GempaViewModel @Inject constructor(
    private val repository: GempaRepository,
    private val networkMonitor: NetworkMonitor,
    private val themeDataStore: ThemeDataStore
) : ViewModel() {

    // ── Search & Filter State ────────────────────────────────────────────────

    /**
     * MutableStateFlow untuk query pencarian.
     * Internal (_searchQuery) hanya bisa diubah dari ViewModel.
     * External (searchQuery) hanya bisa dibaca oleh UI.
     * Ini adalah pola encapsulation yang benar untuk StateFlow.
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * MutableStateFlow untuk filter magnitude minimum.
     * 0.0 = tidak ada filter (tampilkan semua).
     */
    private val _magnitudeFilter = MutableStateFlow(0.0)
    val magnitudeFilter: StateFlow<Double> = _magnitudeFilter.asStateFlow()

    // ── Network State ────────────────────────────────────────────────────────

    /**
     * Status koneksi internet yang diobservasi dari NetworkMonitor.
     * UI akan menampilkan banner offline jika false.
     */
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = networkMonitor.isCurrentlyConnected()
        )

    // ── Theme State ──────────────────────────────────────────────────────────

    val isDarkMode: StateFlow<Boolean> = themeDataStore.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    // ── Gempa Latest State ───────────────────────────────────────────────────

    private val _latestGempaState = MutableStateFlow<UiState<Gempa>>(UiState.Loading)
    val latestGempaState: StateFlow<UiState<Gempa>> = _latestGempaState.asStateFlow()

    // ── Gempa List State ─────────────────────────────────────────────────────

    private val _gempaTerkiniState = MutableStateFlow<UiState<List<Gempa>>>(UiState.Loading)
    val gempaTerkiniState: StateFlow<UiState<List<Gempa>>> = _gempaTerkiniState.asStateFlow()

    /**
     * Daftar gempa yang sudah difilter berdasarkan search query dan magnitude.
     *
     * Menggunakan combine() untuk menggabungkan 3 Flow secara reaktif:
     * - observeAllGempa() dari database (update otomatis saat cache berubah)
     * - searchQuery
     * - magnitudeFilter
     *
     * debounce(300ms): tunggu 300ms setelah user berhenti mengetik
     * sebelum melakukan filter — mengurangi komputasi yang tidak perlu.
     *
     * flatMapLatest: cancel request sebelumnya jika query berubah lagi.
     */
    val filteredGempaList: StateFlow<List<Gempa>> = combine(
        repository.observeAllGempa(),
        _searchQuery.debounce(300),
        _magnitudeFilter
    ) { allGempa, query, minMagnitude ->
        allGempa
            .filter { gempa ->
                // Filter wilayah (case-insensitive)
                (query.isBlank() || gempa.wilayah.contains(query, ignoreCase = true)) &&
                // Filter magnitude
                (minMagnitude <= 0.0 || gempa.magnitude >= minMagnitude)
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // ── Initialization ───────────────────────────────────────────────────────

    init {
        // Fetch data saat ViewModel pertama kali dibuat
        fetchLatestGempa()
        fetchGempaTerkini()
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    /**
     * Mengambil data gempa terbaru dari API.
     * Dipanggil saat init dan saat user pull-to-refresh.
     */
    fun fetchLatestGempa() {
        viewModelScope.launch {
            repository.getLatestGempa().collect { state ->
                _latestGempaState.value = state
                Timber.d("Latest gempa state: $state")
            }
        }
    }

    /**
     * Mengambil daftar gempa terkini dari API.
     */
    fun fetchGempaTerkini() {
        viewModelScope.launch {
            repository.getGempaTerkini().collect { state ->
                _gempaTerkiniState.value = state
                Timber.d("Gempa terkini state: $state")
            }
        }
    }

    /**
     * Refresh semua data (dipanggil dari pull-to-refresh).
     * Langsung set ke Loading agar UI menampilkan indicator.
     */
    fun refreshAll() {
        _latestGempaState.value = UiState.Loading
        _gempaTerkiniState.value = UiState.Loading
        fetchLatestGempa()
        fetchGempaTerkini()
    }

    /**
     * Update query pencarian dari user input.
     * debounce di filteredGempaList akan menangani rate limiting.
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Set filter magnitude minimum.
     * @param magnitude 0.0 untuk reset filter, atau nilai M untuk filter M+
     */
    fun onMagnitudeFilterChange(magnitude: Double) {
        _magnitudeFilter.value = magnitude
    }

    /**
     * Reset semua filter.
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _magnitudeFilter.value = 0.0
    }

    /**
     * Toggle dark/light theme.
     * Disimpan ke DataStore — persisten antar sesi.
     */
    fun toggleTheme() {
        viewModelScope.launch {
            themeDataStore.toggleTheme()
        }
    }
}
