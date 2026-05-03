package dev.moonsetter.shengcat.data.audio

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PitchDetector @Inject constructor() {

    private val sampleRate: Int = 44100
    private val threshold: Float = 0.15f
    private val minFrequency: Float = 80f
    private val maxFrequency: Float = 800f

    fun detect(frame: FloatArray): Float? {
        if (frame.size < 1024) return null

        val halfSize = frame.size / 2
        val cmnd = FloatArray(halfSize)

        cmnd[0] = 1f
        var runningSum = 0f
        for (tau in 1 until halfSize) {
            var diff = 0f
            for (j in 0 until halfSize) {
                val delta = frame[j] - frame[j + tau]
                diff += delta * delta
            }
            runningSum += diff
            cmnd[tau] = if (runningSum == 0f) 0f else diff * tau / runningSum
        }

        var tau = 2
        while (tau < halfSize - 1) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 < halfSize - 1 && cmnd[tau + 1] < cmnd[tau]) {
                    tau++
                }
                break
            }
            tau++
        }
        if (tau >= halfSize - 1) return null

        val refined = if (tau in 1 until halfSize - 1) {
            val prev = cmnd[tau - 1]
            val curr = cmnd[tau]
            val next = cmnd[tau + 1]
            val denom = 2f * (2f * curr - prev - next)
            if (denom == 0f) tau.toFloat()
            else tau + (next - prev) / denom
        } else {
            tau.toFloat()
        }

        if (refined <= 0f) return null
        val frequency = sampleRate / refined
        if (frequency < minFrequency || frequency > maxFrequency) return null

        return frequency
    }
}