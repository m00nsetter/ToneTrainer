package dev.moonsetter.shengcat.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.moonsetter.shengcat.data.model.PracticeMode
import java.time.LocalDate

@Entity(tableName="practice_result_table")
data class PracticeResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val totalQuestions: Int,
    val score: Int,
    val practiceMode: PracticeMode,
    val date: LocalDate
)