package dev.moonsetter.shengcat.data.db

import androidx.room.TypeConverter
import dev.moonsetter.shengcat.model.Language
import dev.moonsetter.shengcat.model.PracticeMode
import dev.moonsetter.shengcat.model.TranslationEngine
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long {
        return date.toEpochDay()
    }
    @TypeConverter
    fun toLocalDate(epoch: Long): LocalDate {
        return LocalDate.ofEpochDay(epoch)
    }

    @TypeConverter
    fun fromLanguage(enumLang: Language): String {
        return enumLang.name
    }
    @TypeConverter
    fun toLanguage(value: String): Language {
        return Language.valueOf(value)
    }

    @TypeConverter
    fun fromTranslationEngine(enumTranslationEngine: TranslationEngine): String {
        return enumTranslationEngine.name
    }
    @TypeConverter
    fun toTranslationEngine(value: String): TranslationEngine {
        return TranslationEngine.valueOf(value)
    }

    @TypeConverter
    fun fromPracticeMode(enumPracticeMode: PracticeMode): String {
        return enumPracticeMode.name
    }
    @TypeConverter
    fun toPracticeMode(value: String): PracticeMode {
        return PracticeMode.valueOf(value)
    }
}