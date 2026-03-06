package dev.moonsetter.shengcat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.moonsetter.shengcat.data.db.entity.TranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Insert
    suspend fun insert(entity: TranslationEntity)

    @Query("SELECT * FROM translation_table ORDER BY date DESC")
    fun getAll(): Flow<List<TranslationEntity>>

    @Query("DELETE FROM translation_table")
    suspend fun deleteAll()
}