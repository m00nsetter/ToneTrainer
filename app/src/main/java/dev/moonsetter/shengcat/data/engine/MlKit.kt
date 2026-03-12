package dev.moonsetter.shengcat.data.engine

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dev.moonsetter.shengcat.model.Language
import dev.moonsetter.shengcat.model.Language.*
import kotlinx.coroutines.suspendCancellableCoroutine

class MlKit : TranslationEngine {
    private fun languageToTranslateLanguage(language: Language): String {
        return when (language) {
            CHINESE -> TranslateLanguage.CHINESE
            ENGLISH -> TranslateLanguage.ENGLISH
            RUSSIAN -> TranslateLanguage.RUSSIAN
        }
    }

    override suspend fun translate(text: String, from: Language, to: Language): TranslationResult {
        return suspendCancellableCoroutine { continuation ->
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(languageToTranslateLanguage(from))
                .setTargetLanguage(languageToTranslateLanguage(to))
                .build()
            val translator = Translation.getClient(options)

            translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
                .addOnSuccessListener {
                    translator.translate(text)
                        .addOnSuccessListener { translatedText ->
                            continuation.resumeWith(Result.success(TranslationResult(translatedText, null, null)))
                            translator.close()
                        }
                        .addOnFailureListener {
                            continuation.resumeWith(Result.success(TranslationResult(translatedText = "", pinyin = null, error = it.message)))
                            translator.close()
                        }
                }
                .addOnFailureListener {
                    continuation.resumeWith(Result.success(TranslationResult(translatedText = "", pinyin = null, error = it.message)))
                    translator.close()
                }

            continuation.invokeOnCancellation {
                translator.close()
            }
        }
    }
}