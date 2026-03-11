package dev.moonsetter.shengcat.data.repository

import dev.moonsetter.shengcat.data.db.dao.TranslationDao
import dev.moonsetter.shengcat.data.db.entity.TranslationEntity
import dev.moonsetter.shengcat.data.engine.MlKit
import dev.moonsetter.shengcat.data.engine.TranslationResult
import dev.moonsetter.shengcat.data.model.Language
import dev.moonsetter.shengcat.data.model.TranslationEngine
import javax.inject.Inject
import javax.inject.Singleton
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import java.time.LocalDate

@Singleton
class TranslationRepository @Inject constructor(
    private val translationDao: TranslationDao,
    private val mlKit: MlKit
) {
    val format: HanyuPinyinOutputFormat = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITH_TONE_MARK
        vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    private fun hanziToPinyin(text: String): String {
        return PinyinHelper.toHanYuPinyinString(
            text, format,
            " ",
            true
        )
    }

    suspend fun translate(text: String, from: Language, to: Language): TranslationResult {
        val result = mlKit.translate(text, from, to)
        if (result.error != null) {
            return result
        }

        val pinyin = if (to == Language.CHINESE) hanziToPinyin(result.translatedText)
        else null

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