package dev.moonsetter.shengcat.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.moonsetter.shengcat.data.repository.PracticeRepository
import dev.moonsetter.shengcat.data.repository.SettingsRepository
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.LANG_EN
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.LANG_RU
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_DARK
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_LIGHT
import dev.moonsetter.shengcat.data.repository.SettingsRepository.Companion.THEME_SYSTEM
import dev.moonsetter.shengcat.data.repository.TranslationRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val language: String = LANG_EN,
    val theme: String = THEME_SYSTEM,
    val showResetDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val translationRepository: TranslationRepository,
    private val practiceRepository: PracticeRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.language,
                settingsRepository.theme
            ) { language, theme ->
                SettingsUiState(language = language, theme = theme)
            }.collect { state ->
                uiState = state
            }
        }
    }

    fun onLanguageSelected(lang: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(lang)
        }
    }

    fun onThemeSelected(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun onResetClick() {
        uiState = uiState.copy(showResetDialog = true)
    }

    fun onResetConfirm() {
        viewModelScope.launch {
            translationRepository.clearHistory()
            practiceRepository.clearHistory()
            uiState = uiState.copy(showResetDialog = false)
        }
    }

    fun onResetDismiss() {
        uiState = uiState.copy(showResetDialog = false)
    }
}