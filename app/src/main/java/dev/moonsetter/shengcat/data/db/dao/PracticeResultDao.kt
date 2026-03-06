package dev.moonsetter.shengcat.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.moonsetter.shengcat.data.db.entity.PracticeResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeResultDao {
    @Insert
    suspend fun insert(entity: PracticeResultEntity)

    @Query("SELECT * FROM practice_result_table ORDER BY date DESC")
    fun getAll(): Flow<List<PracticeResultEntity>>

    @Query("DELETE FROM practice_result_table")
    suspend fun deleteAll()
}