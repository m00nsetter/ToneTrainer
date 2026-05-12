package dev.moonsetter.shengcat.data.audio

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ToneAnalyzer @Inject constructor() {

    // Added a noise floor to prevent normalization from being ruined by silence
    private fun normalize(points: List<Float>): FloatArray {
        val noiseFloor = 60f
        val validPoints = points.filter { it > noiseFloor }

        if (validPoints.isEmpty()) return FloatArray(points.size) { 0f }

        val min = validPoints.min()
        val max = validPoints.max()
        val avg = validPoints.average().toFloat()
        val range = max - min

        // If the pitch is flat (range < 10Hz), don't squash it to 0.5.
        // Determine if it's high or low.
        if (range < 10f) {
            // Assume > 200Hz is High (Tone 1) for most voices, < 200Hz is Low
            val flatValue = if (avg > 200f) 0.9f else 0.3f
            return FloatArray(points.size) { flatValue }
        }

        return FloatArray(points.size) { i ->
            if (points[i] < noiseFloor) 0f
            else ((points[i] - min) / range).coerceIn(0f, 1f)
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
        // Only filter for actual voice
        val voicedContent = recordedPitch.filter { it > 60f }

        // If you are speaking very clearly/fast, 5 points might be too high
        // for a small frameSize. Try lowering this to 3 if Tone 1 still fails.
        if (voicedContent.size < 4) return 0f

        val reference = ToneReference.getContour(toneNumber)
        val smoothed = smooth(voicedContent)
        val normalized = normalize(smoothed)

        val resampled = resample(normalized, 20)
        val referenceResampled = resample(reference, 20)

        val distance = dtw(resampled, referenceResampled)

        // Similarity calculation
        return (1f - (distance * 2f)).coerceIn(0f, 1f)
    }
}