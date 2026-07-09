package com.gempa.id.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gempa.id.core.data.local.dao.GempaDao
import com.gempa.id.core.data.local.entity.GempaEntity

/**
 * GempaDatabase — Room Database untuk caching data gempa lokal.
 *
 * @Database:
 * - entities: semua tabel yang ada di database ini
 * - version: naikkan versi setiap kali ada perubahan skema
 * - exportSchema: true untuk menyimpan history skema (berguna untuk migration)
 *
 * Singleton pattern ditangani oleh Hilt melalui @Singleton di DatabaseModule.
 *
 * Migration strategy:
 * - Development: fallbackToDestructiveMigration() (drop & recreate)
 * - Production: tulis Migration object yang tepat
 */
@Database(
    entities = [GempaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GempaDatabase : RoomDatabase() {

    /**
     * Room men-generate implementasi abstract function ini secara otomatis.
     * Hilt akan menggunakan ini untuk menyediakan GempaDao ke repository.
     */
    abstract fun gempaDao(): GempaDao

    companion object {
        const val DATABASE_NAME = "gempa_db"
    }
}
