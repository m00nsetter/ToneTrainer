package dev.moonsetter.shengcat.data.repository

import dev.moonsetter.shengcat.data.db.dao.PracticeResultDao
import dev.moonsetter.shengcat.data.db.entity.PracticeResultEntity
import dev.moonsetter.shengcat.model.PracticeMode
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeRepository @Inject constructor(
    private val dao: PracticeResultDao
) {
    fun getHistory(): Flow<List<PracticeResultEntity>> = dao.getAll()

    suspend fun save(score: Int, totalQuestions: Int, mode: PracticeMode) {
        dao.insert(
            PracticeResultEntity(
                id = 0,
                score = score,
                totalQuestions = totalQuestions,
                practiceMode = mode,
                date = LocalDateTime.now()
            )
        )
    }

    suspend fun clearHistory() = dao.deleteAll()
}