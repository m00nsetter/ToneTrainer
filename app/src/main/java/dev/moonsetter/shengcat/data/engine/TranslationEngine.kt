package dev.moonsetter.shengcat.data.engine

import dev.moonsetter.shengcat.model.Language

data class TranslationResult(val translatedText: String, val pinyin: String?, val error: String?)

interface TranslationEngine {
    suspend fun translate(text: String, from: Language, to: Language): TranslationResult
}