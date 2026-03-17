package dev.moonsetter.shengcat.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.moonsetter.shengcat.model.Language
import dev.moonsetter.shengcat.model.TranslationEngine
import java.time.LocalDateTime

@Entity(tableName="translation_table")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val text: String,
    val translation: String,
    val pinyin: String?,
    val sourceLang: Language,
    val targetLang: Language,
    val engine: TranslationEngine,
    val date: LocalDateTime
)