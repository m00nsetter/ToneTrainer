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
import dev.moonsetter.shengcat.data.audio.MicrophoneRecorder
import dev.moonsetter.shengcat.data.audio.PitchDetector
import dev.moonsetter.shengcat.data.audio.ToneAnalyzer
import dev.moonsetter.shengcat.data.repository.PracticeRepository
import dev.moonsetter.shengcat.data.repository.SyllableRepository
import dev.moonsetter.shengcat.model.PracticeMode
import dev.moonsetter.shengcat.model.SyllableItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class QuestionResult(
    val syllable: SyllableItem,
    val similarity: Float,
    val recordedPitch: List<Float>
)

data class PronunciationUiState(
    val syllables: List<SyllableItem> = emptyList(),
    val currentIndex: Int = 0,
    val isRecording: Boolean = false,
    val currentPitchPoints: List<Float> = emptyList(),
    val lastResult: QuestionResult? = null,
    val sessionResults: List<QuestionResult> = emptyList(),
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val currentSyllable: SyllableItem? get() = syllables.getOrNull(currentIndex)
    val totalQuestions: Int get() = syllables.size
    val score: Int get() = sessionResults.count { it.similarity >= 0.6f }
    val isLastQuestion: Boolean get() = currentIndex >= syllables.size - 1
}

@HiltViewModel
class PronunciationViewModel @Inject constructor(
    private val recorder: MicrophoneRecorder,
    private val pitchDetector: PitchDetector,
    private val toneAnalyzer: ToneAnalyzer,
    private val repository: PracticeRepository,
    private val syllableRepository: SyllableRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var uiState by mutableStateOf(PronunciationUiState())
        private set

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var recordingJob: Job? = null

    init {
        tts = TextToSpeech(context) { status ->
            isTtsReady = status == TextToSpeech.SUCCESS
        }
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true)
                val pool = syllableRepository.getPool()
                uiState = PronunciationUiState(
                    syllables = pool.shuffled().take(10),
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isLoading = false)
            }
        }
    }

//    fun loadSyllables(syllables: List<SyllableItem>) {
//        uiState = PronunciationUiState(syllables = syllables.shuffled())
//    }

    fun onPlayReferenceClick() {
        val syllable = uiState.currentSyllable ?: return
        if (!isTtsReady) return
        tts?.language = Locale.forLanguageTag("zh-CN")
        tts?.speak(syllable.character, TextToSpeech.QUEUE_FLUSH, null, "tts_reference")
    }

    fun onRecordClick() {
        if (uiState.isRecording) stopRecording()
        else startRecording()
    }

    private fun startRecording() {
        uiState = uiState.copy(isRecording = true, currentPitchPoints = emptyList(), lastResult = null)
        recordingJob = viewModelScope.launch {
            val pitchPoints = mutableListOf<Float>()
            try {
                recorder.record().collect { frame ->
                    val hz = pitchDetector.process(frame)
                    if (hz != null) {
                        pitchPoints.add(hz)
                        uiState = uiState.copy(currentPitchPoints = pitchPoints.toList())
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message, isRecording = false)
            }
        }
    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        val syllable = uiState.currentSyllable ?: run {
            uiState = uiState.copy(isRecording = false)
            return
        }
        val recorded = uiState.currentPitchPoints
        if (recorded.size < 10) {
            uiState = uiState.copy(isRecording = false)
            return
        }
        val similarity = toneAnalyzer.compare(recorded, syllable.toneNumber)
        val result = QuestionResult(
            syllable = syllable,
            similarity = similarity,
            recordedPitch = recorded
        )
        uiState = uiState.copy(
            isRecording = false,
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
                currentPitchPoints = emptyList(),
                lastResult = null
            )
        }
    }

    private fun finishSession() {
        viewModelScope.launch {
            try {
                repository.save(
                    score = uiState.score,
                    totalQuestions = uiState.totalQuestions,
                    mode = PracticeMode.PRONUNCIATION
                )
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message)
            } finally {
                uiState = uiState.copy(isFinished = true)
            }
        }
    }

    fun onRepeatSession() {
        uiState = PronunciationUiState(syllables = uiState.syllables.shuffled())
    }

    override fun onCleared() {
        tts?.shutdown()
        recordingJob?.cancel()
        super.onCleared()
    }
}