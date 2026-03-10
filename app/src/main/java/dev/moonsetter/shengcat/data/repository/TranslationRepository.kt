package dev.moonsetter.shengcat.data.repository

import dev.moonsetter.shengcat.data.db.dao.TranslationDao
import dev.moonsetter.shengcat.data.engine.MlKit
import dev.moonsetter.shengcat.data.engine.TranslationResult
import dev.moonsetter.shengcat.data.model.Language
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepository @Inject constructor(
    private val translationDao: TranslationDao,
    private val mlKit: MlKit
) {
    suspend fun translate(text: String, from: Language, to: Language): TranslationResult {
        val result = mlKit.translate(text, from, to)
//        if (result.error != null) {
            return result
//        }
        // TODO: generate pinyin
        // TODO: save to db
        // TODO: retirn translation with pinyin
    }
    fun getHistory() = translationDao.getAll()
    suspend fun clearHistory() = translationDao.deleteAll()
}