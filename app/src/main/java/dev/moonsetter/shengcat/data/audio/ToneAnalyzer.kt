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

    // медианный фильтр для сглаживания шума питча
    private fun smooth(points: List<Float>, windowSize: Int = 5): List<Float> {
        if (points.size < windowSize) return points
        return points.mapIndexed { i, _ ->
            val half = windowSize / 2
            val from = maxOf(0, i - half)
            val to = minOf(points.size - 1, i + half)
            points.subList(from, to + 1).sorted()[( to - from) / 2]
        }
    }

    // возвращает схожесть 0.0–1.0 между записанным контуром и эталоном
    fun compare(recordedPitch: List<Float>, toneNumber: Int): Float {
        if (recordedPitch.size < 10) return 0f
        val reference = ToneReference.getContour(toneNumber)
        val smoothed = smooth(recordedPitch)           // сначала сглаживаем
        val normalized = normalize(smoothed)           // потом нормализуем
        val resampled = resample(normalized, reference.size)
        val distance = dtw(resampled, reference)
        // нормализация: максимальное DTW при размере 10 = ~3.0 на практике
        val maxDistance = 3.0f
        return (1f - (distance / maxDistance)).coerceIn(0f, 1f)
    }
}