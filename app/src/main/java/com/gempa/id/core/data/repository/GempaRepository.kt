package com.gempa.id.core.data.repository

import com.gempa.id.core.data.local.dao.GempaDao
import com.gempa.id.core.data.remote.api.BmkgApiService
import com.gempa.id.core.network.UiState
import com.gempa.id.feature.gempa.data.toDomain
import com.gempa.id.feature.gempa.data.toDomainList
import com.gempa.id.feature.gempa.data.toEntity
import com.gempa.id.feature.gempa.data.toEntityList
import com.gempa.id.feature.gempa.domain.model.Gempa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GempaRepository — Single Source of Truth untuk data gempa.
 *
 * Repository Pattern adalah komponen arsitektural yang:
 * 1. Mengabstraksi sumber data (API vs Database) dari ViewModel
 * 2. Mengelola logika caching — "offline-first" strategy
 * 3. ViewModel tidak tahu apakah data dari internet atau cache
 *
 * Offline-First Strategy:
 * 1. Tampilkan data cache dari Room (jika ada) → UI langsung responsif
 * 2. Fetch data baru dari API di background
 * 3. Simpan ke Room → Flow di DAO otomatis emit data baru ke UI
 * 4. Jika API gagal, data cache tetap tersedia
 *
 * @Singleton: hanya ada 1 instance repository di seluruh app lifecycle
 */
@Singleton
class GempaRepository @Inject constructor(
    private val apiService: BmkgApiService,
    private val gempaDao: GempaDao
) {

    /**
     * Mendapatkan gempa terbaru (1 event terakhir).
     * Emits: Loading → (cache jika ada) → Success dari network → Error jika gagal
     *
     * Menggunakan Flow untuk reaktivitas — Room akan otomatis
     * memberi tahu UI ketika data berubah tanpa polling.
     */
    fun getLatestGempa(): Flow<UiState<Gempa>> = flow {
        emit(UiState.Loading)

        try {
            // 1. Fetch dari API
            Timber.d("Fetching latest gempa from BMKG API...")
            val response = apiService.getAutoGempa()
            val gempaEntity = response.infoGempa.gempa.toEntity(isLatest = true)

            // 2. Hapus data latest lama, simpan yang baru
            gempaDao.deleteLatestGempa()
            gempaDao.insertGempa(gempaEntity)
            Timber.d("Latest gempa cached: ${gempaEntity.wilayah}")

            emit(UiState.Success(gempaEntity.toDomain()))

        } catch (e: IOException) {
            // IOException: masalah jaringan (no internet, timeout)
            Timber.w(e, "Network error fetching latest gempa")
            emit(UiState.Error(
                message = "Tidak ada koneksi internet. Menampilkan data terakhir yang tersedia.",
                throwable = e
            ))
        } catch (e: Exception) {
            // Exception lain: parsing error, server error, dll
            Timber.e(e, "Error fetching latest gempa")
            emit(UiState.Error(
                message = "Gagal memuat data: ${e.localizedMessage}",
                throwable = e
            ))
        }
    }

    /**
     * Stream data gempa terbaru dari Room Database.
     * Flow ini akan emit ulang setiap kali data di Room berubah.
     * Digunakan untuk menampilkan data cache secara reaktif.
     */
    fun observeLatestGempa(): Flow<Gempa?> =
        gempaDao.getLatestGempa()
            .map { entity -> entity?.toDomain() }
            .catch { e -> Timber.e(e, "Error observing latest gempa") }

    /**
     * Mendapatkan daftar 15 gempa terkini.
     * Sama dengan getLatestGempa() tapi untuk list.
     */
    fun getGempaTerkini(): Flow<UiState<List<Gempa>>> = flow {
        emit(UiState.Loading)

        try {
            Timber.d("Fetching gempa terkini from BMKG API...")
            val response = apiService.getGempaTerkini()
            val entities = response.infoGempa.gempa.toEntityList()

            // Hapus cache lama, simpan yang baru
            gempaDao.deleteAllGempaTerkini()
            gempaDao.insertAll(entities)
            Timber.d("${entities.size} gempa cached")

            val domainList = entities.toDomainList()
            if (domainList.isEmpty()) {
                emit(UiState.Empty)
            } else {
                emit(UiState.Success(domainList))
            }

        } catch (e: IOException) {
            Timber.w(e, "Network error, serving from cache")
            emit(UiState.Error(
                message = "Tidak ada koneksi internet. Menampilkan data cache.",
                throwable = e
            ))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching gempa terkini")
            emit(UiState.Error(
                message = "Gagal memuat data: ${e.localizedMessage}",
                throwable = e
            ))
        }
    }

    /**
     * Observasi semua gempa dari Room (offline cache).
     * Flow reaktif — update otomatis saat Room berubah.
     */
    fun observeAllGempa(): Flow<List<Gempa>> =
        gempaDao.getAllGempa()
            .map { entities -> entities.toDomainList() }
            .catch { e -> Timber.e(e, "Error observing all gempa") }

    /**
     * Pencarian gempa berdasarkan nama wilayah.
     * @param query string pencarian
     */
    fun searchGempa(query: String): Flow<List<Gempa>> =
        gempaDao.searchGempaByWilayah(query)
            .map { entities -> entities.toDomainList() }
            .catch { e -> Timber.e(e, "Error searching gempa") }

    /**
     * Filter gempa berdasarkan magnitude minimum.
     * @param minMagnitude threshold magnitude (contoh: 5.0 untuk M5.0+)
     */
    fun filterByMagnitude(minMagnitude: Double): Flow<List<Gempa>> =
        gempaDao.filterByMagnitude(minMagnitude)
            .map { entities -> entities.toDomainList() }
            .catch { e -> Timber.e(e, "Error filtering gempa") }
}
