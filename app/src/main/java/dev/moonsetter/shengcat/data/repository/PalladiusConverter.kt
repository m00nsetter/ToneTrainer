package dev.moonsetter.shengcat.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.Normalizer

private val pattern = Regex("[\u0300\u0301\u0304\u030C]")
private var palladiusMap: Map<String, String>? = null

private fun loadMap(context: Context): Map<String, String> {
    return palladiusMap ?: run {
        val json = context.assets.open("palladius.json").bufferedReader().readText()
        val type = object : TypeToken<Map<String, String>>() {}.type
        val map: Map<String, String> = Gson().fromJson(json, type)
        palladiusMap = map
        map
    }
}

private fun stripTones(syllable: String): String {
    val normalized = Normalizer.normalize(syllable, Normalizer.Form.NFD)
    return normalized
        .replace(pattern, "")
        .let { Normalizer.normalize(it, Normalizer.Form.NFC) }
}

private fun convertWord(word: String, map: Map<String, String>): String? {
    if (word.isEmpty()) return ""
    for (len in minOf(word.length, 6) downTo 1) {
        val candidate = word.substring(0, len)
        val palladius = map[candidate]
        if (palladius != null) {
            val remainder = convertWord(word.substring(len), map)
            if (remainder != null) return palladius + remainder
        }
    }
    return null
}

fun pinyinToPalladius(pinyin: String, context: Context): String {
    val map = loadMap(context)
    return pinyin.split(" ").joinToString(" ") { word ->
        val punctuation = word.filter { !it.isLetter() }
        val letters = word.filter { it.isLetter() }
        val bare = stripTones(letters).lowercase()
        val converted = convertWord(bare, map) ?: letters
        converted + punctuation
    }
}