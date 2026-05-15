package dev.moonsetter.shengcat.data.audio

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ToneAnalyzer @Inject constructor() {

    private fun normalize(points: List<Float>): FloatArray {
        val validPoints = points.filter { it > 60f }
        if (validPoints.isEmpty()) return FloatArray(points.size) { 0f }

        val min = validPoints.min()
        val max = validPoints.max()
        val range = max - min
        val avg = validPoints.average().toFloat()

        if (range < 10f) {
            val level = if (avg > 200f) 0.9f else 0.4f
            return FloatArray(points.size) { level }
        }

        return FloatArray(points.size) { i ->
            ((points[i] - min) / range).coerceIn(0f, 1f)
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
        return cost[n - 1][m - 1] / (n + m)
    }

    fun compare(recordedPitch: List<Float>, toneNumber: Int): Float {
        val voiced = recordedPitch.filter { it > 60f }
        if (voiced.size < 5) return 0f

        val normalized = normalize(voiced)
        val resampled = resample(normalized, 10)
        val reference = ToneReference.getContour(toneNumber)

        val recordedSlope = resampled.last() - resampled.first()
        val referenceSlope = reference.last() - reference.first()

        val directionPenalty = if (recordedSlope * referenceSlope < -0.1f) 0.4f else 1.0f

        val distance = dtw(resampled, reference)
        val shapeSimilarity = (1f - (distance * 1.5f)).coerceIn(0f, 1f)

        return (shapeSimilarity * directionPenalty).coerceIn(0f, 1f)
    }
}