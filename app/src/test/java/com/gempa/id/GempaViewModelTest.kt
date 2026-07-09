package com.gempa.id.feature.gempa.presentation.viewmodel

import app.cash.turbine.test
import com.gempa.id.core.data.local.datastore.ThemeDataStore
import com.gempa.id.core.data.repository.GempaRepository
import com.gempa.id.core.network.UiState
import com.gempa.id.core.util.NetworkMonitor
import com.gempa.id.feature.gempa.domain.model.Gempa
import com.gempa.id.feature.gempa.domain.model.MagnitudeCategory
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit test untuk GempaViewModel.
 *
 * Prinsip pengujian ViewModel:
 * 1. Mock semua dependency (Repository, NetworkMonitor, DataStore)
 * 2. Test hanya satu unit (ViewModel) secara terisolasi
 * 3. Gunakan TestDispatcher untuk kontrol coroutine
 * 4. Turbine untuk test Flow secara berurutan
 *
 * TestCoroutineDispatcher: menggantikan Dispatchers.Main di unit test
 * (test environment tidak memiliki Android Main Looper).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GempaViewModelTest {

    // Test dispatcher untuk kontrol coroutine di test
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mocks — menggantikan implementasi nyata dengan fake yang bisa dikontrol
    private val repository    = mockk<GempaRepository>()
    private val networkMonitor = mockk<NetworkMonitor>()
    private val themeDataStore = mockk<ThemeDataStore>()

    private lateinit var viewModel: GempaViewModel

    // Data dummy untuk test
    private val dummyGempa = Gempa(
        dateTime    = "2025-01-10T14:30:00+07:00",
        tanggal     = "10 Jan 2025",
        jam         = "14:30:00 WIB",
        coordinates = "-8.50,115.20",
        lintang     = "-8.50 LS",
        bujur       = "115.20 BT",
        magnitude   = 5.2,
        kedalaman   = "10 km",
        wilayah     = "Bali",
        potensi     = "Tidak berpotensi tsunami",
        dirasakan   = "III MMI di Denpasar",
        shakemapUrl = "https://data.bmkg.go.id/test.jpg",
        isLatest    = true
    )

    @Before
    fun setUp() {
        // Set Main dispatcher ke TestDispatcher
        Dispatchers.setMain(testDispatcher)

        // Setup default mock responses
        every { networkMonitor.isOnline } returns flowOf(true)
        every { networkMonitor.isCurrentlyConnected() } returns true
        every { themeDataStore.isDarkMode } returns flowOf(false)
        every { repository.observeAllGempa() } returns flowOf(listOf(dummyGempa))
        every { repository.getLatestGempa() } returns flowOf(UiState.Success(dummyGempa))
        every { repository.getGempaTerkini() } returns flowOf(UiState.Success(listOf(dummyGempa)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init should trigger fetch and emit Success state for latest gempa`() = runTest {
        // Given
        every { repository.getLatestGempa() } returns flowOf(UiState.Success(dummyGempa))

        // When
        viewModel = GempaViewModel(repository, networkMonitor, themeDataStore)

        // Then — gunakan Turbine untuk test Flow
        viewModel.latestGempaState.test {
            val state = awaitItem()
            assertTrue("State harus Success", state is UiState.Success)
            assertEquals("Data gempa harus cocok", dummyGempa, (state as UiState.Success).data)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should emit Error state when network fails`() = runTest {
        // Given — simulasi error jaringan
        val errorMessage = "Tidak ada koneksi internet."
        every { repository.getLatestGempa() } returns flowOf(
            UiState.Error(message = errorMessage)
        )
        every { repository.getGempaTerkini() } returns flowOf(
            UiState.Error(message = errorMessage)
        )

        // When
        viewModel = GempaViewModel(repository, networkMonitor, themeDataStore)

        // Then
        viewModel.latestGempaState.test {
            val state = awaitItem()
            assertTrue("State harus Error", state is UiState.Error)
            assertEquals(errorMessage, (state as UiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should filter gempa list by wilayah`() = runTest {
        // Given — dua gempa dengan wilayah berbeda
        val gempaJawa = dummyGempa.copy(wilayah = "Jawa Tengah", dateTime = "dt1")
        val gempaBali = dummyGempa.copy(wilayah = "Bali", dateTime = "dt2")
        every { repository.observeAllGempa() } returns flowOf(listOf(gempaJawa, gempaBali))
        viewModel = GempaViewModel(repository, networkMonitor, themeDataStore)

        // When — search "Bali"
        viewModel.onSearchQueryChange("Bali")

        // Then — hanya gempa Bali yang muncul
        viewModel.filteredGempaList.test {
            // Skip initial empty emission
            val result = awaitItem()
            // Setelah debounce dan filter
            assertTrue("Harus mengandung Bali", result.any { it.wilayah == "Bali" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `magnitudeFilter should only show gempa above threshold`() = runTest {
        // Given
        val gempaKecil = dummyGempa.copy(magnitude = 2.5, dateTime = "dt1")
        val gempaBesar = dummyGempa.copy(magnitude = 6.0, dateTime = "dt2")
        every { repository.observeAllGempa() } returns flowOf(listOf(gempaKecil, gempaBesar))
        viewModel = GempaViewModel(repository, networkMonitor, themeDataStore)

        // When — filter M5+
        viewModel.onMagnitudeFilterChange(5.0)

        // Then — hanya gempa 6.0 yang lolos
        viewModel.filteredGempaList.test {
            val result = awaitItem()
            assertFalse("Gempa 2.5 harus difilter", result.any { it.magnitude < 5.0 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearFilters should reset searchQuery and magnitudeFilter`() = runTest {
        // Given
        viewModel = GempaViewModel(repository, networkMonitor, themeDataStore)
        viewModel.onSearchQueryChange("Bali")
        viewModel.onMagnitudeFilterChange(5.0)

        // When
        viewModel.clearFilters()

        // Then
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(0.0, viewModel.magnitudeFilter.value, 0.001)
    }

    @Test
    fun `isOnline should reflect network monitor state`() = runTest {
        // Given — simulasi offline
        every { networkMonitor.isOnline } returns flowOf(false)
        every { networkMonitor.isCurrentlyConnected() } returns false

        // When
        viewModel = GempaViewModel(repository, networkMonitor, themeDataStore)

        // Then
        viewModel.isOnline.test {
            assertFalse("Harus offline", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Gempa magnitudeCategory should return correct category`() {
        // Test pure domain logic — tidak butuh coroutine
        assertEquals(MagnitudeCategory.MICRO,    dummyGempa.copy(magnitude = 2.5).magnitudeCategory)
        assertEquals(MagnitudeCategory.MINOR,    dummyGempa.copy(magnitude = 3.5).magnitudeCategory)
        assertEquals(MagnitudeCategory.LIGHT,    dummyGempa.copy(magnitude = 4.5).magnitudeCategory)
        assertEquals(MagnitudeCategory.MODERATE, dummyGempa.copy(magnitude = 5.5).magnitudeCategory)
        assertEquals(MagnitudeCategory.STRONG,   dummyGempa.copy(magnitude = 6.5).magnitudeCategory)
        assertEquals(MagnitudeCategory.MAJOR,    dummyGempa.copy(magnitude = 7.5).magnitudeCategory)
        assertEquals(MagnitudeCategory.GREAT,    dummyGempa.copy(magnitude = 8.5).magnitudeCategory)
    }

    @Test
    fun `hasTsunamiPotential should be false when potensi contains Tidak`() {
        val safe = dummyGempa.copy(potensi = "Tidak berpotensi tsunami")
        val danger = dummyGempa.copy(potensi = "Berpotensi menimbulkan tsunami")

        assertFalse(safe.hasTsunamiPotential)
        assertTrue(danger.hasTsunamiPotential)
    }
}
