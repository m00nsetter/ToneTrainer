package dev.moonsetter.shengcat.data.repository

import dev.moonsetter.shengcat.data.db.dao.TranslationDao
import dev.moonsetter.shengcat.data.db.entity.TranslationEntity
import dev.moonsetter.shengcat.data.engine.MlKit
import dev.moonsetter.shengcat.data.engine.TranslationResult
import dev.moonsetter.shengcat.model.Language
import dev.moonsetter.shengcat.model.TranslationEngine
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate

@Singleton
class TranslationRepository @Inject constructor(
    private val translationDao: TranslationDao,
    private val mlKit: MlKit // TODO: сделать переводчик на выбор
) {
    suspend fun translate(text: String, from: Language, to: Language): TranslationResult {
        val result = mlKit.translate(text, from, to)
        if (result.error != null) {
            return result
        }

        val pinyin = when {
            from == Language.CHINESE -> hanziToPinyin(text)
            to == Language.CHINESE -> hanziToPinyin(result.translatedText)
            else -> null
        }

        translationDao.insert(
            TranslationEntity(
                id = 0,
                text = text,
                translation = result.translatedText,
                pinyin = pinyin,
                sourceLang = from,
                targetLang = to,
                engine = TranslationEngine.MLKIT,
                date = LocalDate.now()
            )
        )

        return result.copy(pinyin = pinyin)
    }

    fun getHistory() = translationDao.getAll()
    suspend fun clearHistory() = translationDao.deleteAll()
}