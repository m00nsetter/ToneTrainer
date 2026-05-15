package dev.moonsetter.shengcat.data.audio

object ToneReference {
    val tone1 = floatArrayOf(0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f, 0.9f)
    val tone2 = floatArrayOf(0.3f, 0.35f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.87f, 0.93f, 1.0f)
    val tone3 = floatArrayOf(0.4f, 0.3f, 0.2f, 0.15f, 0.1f, 0.1f, 0.15f, 0.25f, 0.35f, 0.45f)
    val tone4 = floatArrayOf(1.0f, 0.9f, 0.78f, 0.65f, 0.52f, 0.4f, 0.28f, 0.18f, 0.1f, 0.05f)

    fun getContour(toneNumber: Int): FloatArray = when (toneNumber) {
        1 -> tone1
        2 -> tone2
        3 -> tone3
        4 -> tone4
        else -> throw IllegalArgumentException("Недопустимый номер тона: $toneNumber")
    }
}