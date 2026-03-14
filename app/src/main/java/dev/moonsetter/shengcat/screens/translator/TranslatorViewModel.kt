package dev.moonsetter.shengcat.screens.translator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.moonsetter.shengcat.data.repository.TranslationRepository
import dev.moonsetter.shengcat.model.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TranslatorUiState (
    // TODO: добавить выбор переводчика тут или в настройках
    val sourceLang: Language = Language.RUSSIAN, // TODO: в настройках пользователь выбирает язык приложения, дефолт значение из shared preference подставляется сюда
    val targetLang: Language = Language.CHINESE,
    val inputText: String = "",
    val translatedText: String = "",
    val pinyin: String? = null,
    val translationError: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class TranslatorViewModel @Inject constructor(val repository: TranslationRepository): ViewModel() {
    var uiState by mutableStateOf(TranslatorUiState())
    private set

    fun onLanguageSwapClick() {
        uiState = if (uiState.translatedText.isBlank()) {
            uiState.copy(
                sourceLang = uiState.targetLang,
                targetLang = uiState.sourceLang,
                inputText = ""
            )
        } else {
            uiState.copy(
                sourceLang = uiState.targetLang,
                targetLang = uiState.sourceLang,
                inputText = uiState.translatedText,
                translatedText = uiState.inputText
            )
        }
    }

    fun onSourceLanguageChange(language: Language) {
        uiState = uiState.copy(sourceLang = language)
    }

    fun onTargetLanguageChange(language: Language) {
        uiState = uiState.copy(targetLang = language)
    }

    fun onInputTextChange(newText: String) {
        uiState = uiState.copy(inputText = newText)
    }

    fun onTranslateClick() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, translationError = null)
                val result = repository.translate(uiState.inputText, uiState.sourceLang, uiState.targetLang)
                uiState = if (result.error == null) {
                    uiState.copy(
                        translatedText = result.translatedText,
                        pinyin = result.pinyin
                    )
                } else {
                    uiState.copy(
                        translatedText = "",
                        pinyin = null,
                        translationError = result.error)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(translationError = e.message)
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun onTtsClick(text: String, language: Language) {
        // TODO: реализовать TTS
    }

    fun onClearClick() {
        uiState = uiState.copy(
            inputText = "",
            translatedText = "",
            pinyin = null,
            translationError = null
        )
    }
}