package dev.moonsetter.shengcat.screens.translator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.moonsetter.shengcat.data.db.entity.TranslationEntity
import dev.moonsetter.shengcat.data.repository.TranslationRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TranslatorHistoryUiState(
    val translationHistory: List<TranslationEntity> = emptyList(),
    val showConfirmDialog: Boolean = false
)

@HiltViewModel
class TranslatorHistoryViewModel @Inject constructor(
    private val repository: TranslationRepository
) : ViewModel() {
    var uiState by mutableStateOf(TranslatorHistoryUiState())
        private set

    init {
        viewModelScope.launch {
            repository.getHistory().collect { history ->
                uiState = uiState.copy(translationHistory = history)
            }
        }
    }

    fun onClearClick() {
        uiState = uiState.copy(showConfirmDialog = true)
    }

    fun onClearConfirmed() {
        viewModelScope.launch {
            repository.clearHistory()
            uiState = uiState.copy(showConfirmDialog = false)
        }
    }

    fun onClearDismissed() {
        uiState = uiState.copy(showConfirmDialog = false)
    }
}