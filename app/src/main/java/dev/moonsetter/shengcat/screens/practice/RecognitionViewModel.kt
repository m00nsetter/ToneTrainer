package dev.moonsetter.shengcat.screens.practice

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.moonsetter.shengcat.data.repository.PracticeRepository
import dev.moonsetter.shengcat.data.repository.SyllableRepository
import dev.moonsetter.shengcat.model.PracticeMode
import dev.moonsetter.shengcat.model.SyllableItem
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class RecognitionQuestionResult(
    val syllable: SyllableItem,
    val selectedTone: Int,
    val isCorrect: Boolean
)

data class RecognitionUiState(
    val syllables: List<SyllableItem> = emptyList(),
    val currentIndex: Int = 0,
    val selectedTone: Int? = null,
    val lastResult: RecognitionQuestionResult? = null,
    val sessionResults: List<RecognitionQuestionResult> = emptyList(),
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentSyllable: SyllableItem? get() = syllables.getOrNull(currentIndex)
    val totalQuestions: Int get() = syllables.size
    val score: Int get() = sessionResults.count { it.isCorrect }
    val isLastQuestion: Boolean get() = currentIndex >= syllables.size - 1
}

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val syllableRepository: SyllableRepository,
    private val practiceRepository: PracticeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var uiState by mutableStateOf(RecognitionUiState())
        private set

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            isTtsReady = status == TextToSpeech.SUCCESS
        }
        viewModelScope.launch {
            try {
                val pool = syllableRepository.getPool()
                uiState = RecognitionUiState(
                    syllables = pool.shuffled().take(10),
                    isLoading = false
                )
                // автоматически озвучиваем первый слог
                playCurrentSyllable()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun onPlayClick() {
        playCurrentSyllable()
    }

    fun onToneSelected(tone: Int) {
        val syllable = uiState.currentSyllable ?: return
        // запрещаем повторный выбор после ответа
        if (uiState.lastResult != null) return

        val isCorrect = tone == syllable.toneNumber
        val result = RecognitionQuestionResult(
            syllable = syllable,
            selectedTone = tone,
            isCorrect = isCorrect
        )
        uiState = uiState.copy(
            selectedTone = tone,
            lastResult = result,
            sessionResults = uiState.sessionResults + result
        )
    }

    fun onNextClick() {
        if (uiState.isLastQuestion) {
            finishSession()
        } else {
            uiState = uiState.copy(
                currentIndex = uiState.currentIndex + 1,
                selectedTone = null,
                lastResult = null
            )
            playCurrentSyllable()
        }
    }

    fun onRepeatSession() {
        viewModelScope.launch {
            try {
                val pool = syllableRepository.getPool()
                uiState = RecognitionUiState(
                    syllables = pool.shuffled().take(10),
                    isLoading = false
                )
                playCurrentSyllable()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }

    private fun playCurrentSyllable() {
        val syllable = uiState.currentSyllable ?: return
        if (!isTtsReady) return
        tts?.language = Locale.forLanguageTag("zh-CN")
        tts?.speak(syllable.character, TextToSpeech.QUEUE_FLUSH, null, "tts_recognition")
    }

    private fun finishSession() {
        viewModelScope.launch {
            try {
                practiceRepository.save(
                    score = uiState.score,
                    totalQuestions = uiState.totalQuestions,
                    mode = PracticeMode.RECOGNITION
                )
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            } finally {
                uiState = uiState.copy(isFinished = true)
            }
        }
    }

    override fun onCleared() {
        tts?.shutdown()
        super.onCleared()
    }
}