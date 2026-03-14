package dev.moonsetter.shengcat.data.repository

import com.huaban.analysis.jieba.JiebaSegmenter
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

private val pattern = Regex("[\\u4E00-\\u9FFF]+")

private fun fixToneMarks(text: String): String {
    return text
        .replace('ă', 'ǎ')
        .replace('ĕ', 'ě')
        .replace('ĭ', 'ǐ')
        .replace('ŏ', 'ǒ')
        .replace('ŭ', 'ǔ')
        .replace('Ă', 'Ǎ')
        .replace('Ĕ', 'Ě')
        .replace('Ĭ', 'Ǐ')
        .replace('Ŏ', 'Ǒ')
        .replace('Ŭ', 'Ǔ')
}

private fun fixPunctuation(text: String): String {
    return text
        .replace('。', '.')
        .replace('，', ',')
        .replace('、', ',')
        .replace('？', '?')
        .replace('！', '!')
        .replace('：', ':')
        .replace('；', ';')
        .replace('（', '(')
        .replace('）', ')')
        .replace('《', '«')
        .replace('》', '»')
        .replace('「', '«')
        .replace('」', '»')
        .replace('【', '[')
        .replace('】', ']')
}

private fun cleanupSpacing(text: String): String {
    return text
        .replace(Regex("\\s+"), " ")
        .replace(Regex(" ([。，、？！：；）》”」】（《“「【.,?!:;)»\\]])"), "$1")
        .replace(Regex("([。，、？！：；（《“「【）》”」】#(«\\[]) "), "$1")
        .trim()
}

private val format: HanyuPinyinOutputFormat = HanyuPinyinOutputFormat().apply {
    caseType = HanyuPinyinCaseType.LOWERCASE
    toneType = HanyuPinyinToneType.WITH_TONE_MARK
    vCharType = HanyuPinyinVCharType.WITH_U_UNICODE
}

private val segmenter = JiebaSegmenter()

private fun charToPinyin(char: Char): String {
    val readings = PinyinHelper.toHanyuPinyinStringArray(char, format)
    return readings?.firstOrNull() ?: char.toString()
}

private fun wordToPinyin(word: String): String {
    return word.map { char ->
        if (pattern.matches(char.toString())) charToPinyin(char)
        else char.toString()
    }.joinToString("")
}

fun hanziToPinyin(text: String): String {
    if (!pattern.containsMatchIn(text)) return text

    val segments = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH)

    return segments.joinToString(" ") { segment ->
        val word = segment.word
        if (pattern.containsMatchIn(word)) {
            wordToPinyin(word)
        } else {
            word.trim()
        }
    }.trim().let { fixToneMarks(it) }
            .let { fixPunctuation(it) }
            .let { cleanupSpacing(it) }
}