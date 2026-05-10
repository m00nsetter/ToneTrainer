package dev.moonsetter.shengcat.screens.practice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.moonsetter.shengcat.data.db.entity.PracticeResultEntity
import dev.moonsetter.shengcat.data.repository.PracticeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PracticeHistoryUiState(
    val results: List<PracticeResultEntity> = emptyList()
)

@HiltViewModel
class PracticeHistoryViewModel @Inject constructor(
    private val repository: PracticeRepository
) : ViewModel() {

    var uiState by mutableStateOf(PracticeHistoryUiState())
        private set

    init {
        viewModelScope.launch {
            repository.getHistory().collect { results ->
                uiState = uiState.copy(results = results)
            }
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}