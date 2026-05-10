package dev.moonsetter.shengcat.data.audio

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PitchDetector @Inject constructor() {

    private val sampleRate: Int = 44100
    private val frameSize: Int = 2048
    private val minFrequency: Float = 80f
    private val maxFrequency: Float = 800f

    private val format = TarsosDSPAudioFormat(
        sampleRate.toFloat(),
        16,
        1,
        true,
        false
    )

    private var lastPitch: Float? = null

    private val pitchProcessor = PitchProcessor(
        PitchEstimationAlgorithm.YIN,
        sampleRate.toFloat(),
        frameSize,
        PitchDetectionHandler { result, _ ->
            lastPitch = if (result.isPitched) result.pitch else null
        }
    )

    fun process(frame: FloatArray): Float? {
        val audioEvent = AudioEvent(format)
        audioEvent.floatBuffer = frame
        pitchProcessor.process(audioEvent)
        val pitch = lastPitch ?: return null
        if (pitch !in minFrequency..maxFrequency) return null
        return pitch
    }
}