package com.gempa.id.core.data.local.dao

import androidx.room.*
import com.gempa.id.core.data.local.entity.GempaEntity
import kotlinx.coroutines.flow.Flow

/**
 * GempaDao — Data Access Object untuk operasi database gempa.
 *
 * Room men-generate implementasi SQL pada compile time berdasarkan anotasi ini.
 * Semua query yang menggunakan Flow akan otomatis emit nilai baru ketika
 * data di database berubah — reaktif tanpa polling.
 *
 * Pola desain:
 * - Flow<List<T>>: untuk observasi data yang terus berubah (daftar gempa)
 * - suspend fun: untuk operasi satu kali (insert, delete)
 */
@Dao
interface GempaDao {

    /**
     * Mengamati semua gempa dari database, diurutkan dari terbaru.
     * Flow akan emit list baru setiap kali ada perubahan data.
     * Digunakan di GempaListScreen untuk menampilkan daftar.
     */
    @Query("SELECT * FROM gempa_table ORDER BY cachedAt DESC")
    fun getAllGempa(): Flow<List<GempaEntity>>

    /**
     * Mengambil gempa yang ditandai sebagai "terbaru" (isLatest = true).
     * Digunakan di GempaLatestCard di halaman utama.
     */
    @Query("SELECT * FROM gempa_table WHERE isLatest = 1 LIMIT 1")
    fun getLatestGempa(): Flow<GempaEntity?>

    /**
     * Mencari gempa berdasarkan kata kunci di kolom wilayah.
     * Digunakan untuk fitur Search & Filter.
     * LIKE '%:query%' untuk pencarian parsial.
     */
    @Query("""
        SELECT * FROM gempa_table 
        WHERE wilayah LIKE '%' || :query || '%' 
        ORDER BY cachedAt DESC
    """)
    fun searchGempaByWilayah(query: String): Flow<List<GempaEntity>>

    /**
     * Mencari gempa berdasarkan filter magnitude minimum.
     */
    @Query("""
        SELECT * FROM gempa_table 
        WHERE magnitude >= :minMagnitude 
        ORDER BY magnitude DESC
    """)
    fun filterByMagnitude(minMagnitude: Double): Flow<List<GempaEntity>>

    /**
     * Menyimpan list gempa ke database.
     * OnConflictStrategy.REPLACE: jika dateTime sudah ada, data lama diganti.
     * Ini adalah strategi upsert — insert atau update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gempaList: List<GempaEntity>)

    /**
     * Menyimpan satu gempa (untuk autogempa / gempa terbaru).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGempa(gempa: GempaEntity)

    /**
     * Menghapus semua data gempa yang bukan "latest" untuk refresh cache.
     */
    @Query("DELETE FROM gempa_table WHERE isLatest = 0")
    suspend fun deleteAllGempaTerkini()

    /**
     * Menghapus gempa terbaru untuk di-replace dengan data baru.
     */
    @Query("DELETE FROM gempa_table WHERE isLatest = 1")
    suspend fun deleteLatestGempa()

    /**
     * Mengembalikan jumlah total gempa di cache.
     * Digunakan untuk validasi apakah cache perlu diisi.
     */
    @Query("SELECT COUNT(*) FROM gempa_table")
    suspend fun getGempaCount(): Int
}
