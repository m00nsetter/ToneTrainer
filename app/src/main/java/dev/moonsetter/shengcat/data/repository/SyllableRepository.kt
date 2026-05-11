package dev.moonsetter.shengcat.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.moonsetter.shengcat.model.SyllableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyllableRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var cachedPool: List<SyllableItem>? = null

    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITH_TONE_MARK
        vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
    }

    suspend fun getPool(): List<SyllableItem> = withContext(Dispatchers.IO) {
        cachedPool?.let { return@withContext it }

        val json = context.assets.open("characters.json")
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<String>>() {}.type
        val characters: List<String> = Gson().fromJson(json, type)

        val pool = mutableListOf<SyllableItem>()

        for (character in characters) {
            if (character.length != 1) continue

            val char = character[0]

            // pinyin4j возвращает null если символ не распознан
            val readings = PinyinHelper.toHanyuPinyinStringArray(char, format)
            if (readings.isNullOrEmpty()) continue

            // пропускаем полифоны — несколько разных чтений
            val uniqueReadings = readings.toSet()
            if (uniqueReadings.size > 1) continue

            val pinyin = fixToneMarks(uniqueReadings.first())
            val toneNumber = extractTone(pinyin)
            if (toneNumber == 0) continue // нейтральный тон — пропускаем

            pool.add(SyllableItem(character, pinyin, toneNumber))
        }

        cachedPool = pool
        pool
    }

    // извлекает номер тона из пиньинь с диакритикой (ā=1, á=2, ǎ=3, à=4)
    private fun extractTone(pinyin: String): Int {
        for (char in pinyin) {
            when (char) {
                'ā', 'ē', 'ī', 'ō', 'ū', 'ǖ' -> return 1
                'á', 'é', 'í', 'ó', 'ú', 'ǘ' -> return 2
                'ǎ', 'ě', 'ǐ', 'ǒ', 'ǔ', 'ǚ' -> return 3
                'à', 'è', 'ì', 'ò', 'ù', 'ǜ' -> return 4
            }
        }
        return 0
    }

    // те же фиксы что и в PinyinConverter
    private fun fixToneMarks(text: String): String {
        return text
            .replace('ă', 'ǎ')
            .replace('ĕ', 'ě')
            .replace('ĭ', 'ǐ')
            .replace('ŏ', 'ǒ')
            .replace('ŭ', 'ǔ')
    }
}