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
    private val minFrequency: Float = 60f
    private val maxFrequency: Float = 1000f

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
            // Only accept pitch if it has a reasonable probability of being a voice
            // result.probability > 0.8f is usually a good threshold for clean audio
            lastPitch = if (result.pitch > 0f && result.probability > 0.75f) {
                result.pitch
            } else {
                null
            }
        }
    )

    fun process(frame: FloatArray): Float? {
        val audioEvent = AudioEvent(format)
        audioEvent.floatBuffer = frame
        // Reset lastPitch before processing to ensure we aren't getting old data
        lastPitch = null
        pitchProcessor.process(audioEvent)

        // Filter results to human speech range (60Hz - 1000Hz)
        return lastPitch?.takeIf { it in minFrequency..maxFrequency }
    }
}