package dev.moonsetter.shengcat.data.audio

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ToneAnalyzer @Inject constructor() {

    // Added a noise floor to prevent normalization from being ruined by silence
    private fun normalize(points: List<Float>): FloatArray {
        val noiseFloor = 60f
        val filteredPoints = points.map { if (it < noiseFloor) 0f else it }

        val validPoints = filteredPoints.filter { it > 0f }
        if (validPoints.isEmpty()) return FloatArray(points.size) { 0.5f }

        val min = validPoints.min()
        val max = validPoints.max()
        val range = max - min

        return if (range < 1f) {
            FloatArray(points.size) { 0.5f }
        } else {
            FloatArray(points.size) { i ->
                if (points[i] < noiseFloor) 0.5f
                else ((points[i] - min) / range).coerceIn(0f, 1f)
            }
        }
    }

    private fun resample(points: FloatArray, targetSize: Int): FloatArray {
        if (points.size == targetSize) return points
        return FloatArray(targetSize) { i ->
            val pos = i.toFloat() * (points.size - 1) / (targetSize - 1)
            val lo = pos.toInt().coerceAtMost(points.size - 2)
            val hi = lo + 1
            val frac = pos - lo
            points[lo] * (1f - frac) + points[hi] * frac
        }
    }

    private fun dtw(a: FloatArray, b: FloatArray): Float {
        val n = a.size
        val m = b.size
        val cost = Array(n) { FloatArray(m) { Float.MAX_VALUE } }

        cost[0][0] = abs(a[0] - b[0])
        for (i in 1 until n) cost[i][0] = cost[i - 1][0] + abs(a[i] - b[0])
        for (j in 1 until m) cost[0][j] = cost[0][j - 1] + abs(a[0] - b[j])

        for (i in 1 until n) {
            for (j in 1 until m) {
                val weight = abs(a[i] - b[j])
                cost[i][j] = weight + minOf(cost[i - 1][j], cost[i][j - 1], cost[i - 1][j - 1])
            }
        }
        // Normalize distance by path length
        return cost[n - 1][m - 1] / (n + m)
    }

    private fun smooth(points: List<Float>, windowSize: Int = 5): List<Float> {
        if (points.size < windowSize) return points
        return List(points.size) { i ->
            val half = windowSize / 2
            val from = maxOf(0, i - half)
            val to = minOf(points.size - 1, i + half)
            points.subList(from, to + 1).sorted()[(to - from) / 2]
        }
    }

    fun compare(recordedPitch: List<Float>, toneNumber: Int): Float {
        // Step 1: Remove silence and check if we have enough "voice"
        val voicedContent = recordedPitch.filter { it > 60f }
        if (voicedContent.size < 5) return 0f

        // Step 2: Duration Check (Each frame is ~46ms with 2048 frameSize)
        // Natural syllables are roughly 4 to 15 frames long.
        val frameCount = voicedContent.size
        val durationPenalty = when {
            frameCount > 20 -> 0.6f // Way too long (the "chuaaaaaan" issue)
            frameCount < 4 -> 0.8f  // Too short/staccato
            else -> 1.0f            // Perfect natural length
        }

        val reference = ToneReference.getContour(toneNumber)
        val smoothed = smooth(voicedContent)
        val normalized = normalize(smoothed)

        // Step 3: Resample for DTW comparison
        val resampled = resample(normalized, 20)
        val referenceResampled = resample(reference, 20)

        val distance = dtw(resampled, referenceResampled)

        // Step 4: Final Score calculation with penalty
        val shapeSimilarity = (1f - (distance * 2f)).coerceIn(0f, 1f)
        return (shapeSimilarity * durationPenalty).coerceIn(0f, 1f)
    }

    private fun trimLeadingSilence(points: List<Float>): List<Float> {
        return points.dropWhile { it < 60f } // Drop everything below the noise floor
    }
}