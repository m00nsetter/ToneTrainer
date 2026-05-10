package dev.moonsetter.shengcat.data.audio

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToneAnalyzer @Inject constructor() {

    // нормализует список pitch-значений в диапазон 0.0–1.0
    private fun normalize(points: List<Float>): FloatArray {
        val min = points.min()
        val max = points.max()
        val range = max - min
        return if (range == 0f) FloatArray(points.size) { 0.5f }
        else FloatArray(points.size) { i -> (points[i] - min) / range }
    }

    // ресемплирует массив до нужного размера через линейную интерполяцию
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

    // DTW расстояние между двумя массивами одинаковой длины
    private fun dtw(a: FloatArray, b: FloatArray): Float {
        val n = a.size
        val cost = Array(n) { FloatArray(n) { Float.MAX_VALUE } }
        cost[0][0] = kotlin.math.abs(a[0] - b[0])
        for (i in 1 until n) cost[i][0] = cost[i - 1][0] + kotlin.math.abs(a[i] - b[0])
        for (j in 1 until n) cost[0][j] = cost[0][j - 1] + kotlin.math.abs(a[0] - b[j])
        for (i in 1 until n) {
            for (j in 1 until n) {
                cost[i][j] = kotlin.math.abs(a[i] - b[j]) + minOf(
                    cost[i - 1][j],
                    cost[i][j - 1],
                    cost[i - 1][j - 1]
                )
            }
        }
        return cost[n - 1][n - 1]
    }

    // возвращает схожесть 0.0–1.0 между записанным контуром и эталоном
    fun compare(recordedPitch: List<Float>, toneNumber: Int): Float {
        if (recordedPitch.size < 10) return 0f
        val reference = ToneReference.getContour(toneNumber)
        val normalized = normalize(recordedPitch)
        val resampled = resample(normalized, reference.size)
        val distance = dtw(resampled, reference)
        // максимально допустимое расстояние при размере 10 точек = 10 * 1.0
        val maxDistance = reference.size.toFloat()
        return (1f - (distance / maxDistance)).coerceIn(0f, 1f)
    }
}