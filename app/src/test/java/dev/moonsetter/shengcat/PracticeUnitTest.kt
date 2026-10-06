package dev.moonsetter.shengcat

import dev.moonsetter.shengcat.data.audio.ToneAnalyzer
import dev.moonsetter.shengcat.data.audio.ToneReference
import dev.moonsetter.shengcat.model.SyllableItem
import dev.moonsetter.shengcat.model.pinyinWithTone
import org.junit.Assert.*
import org.junit.Test

class PracticeUnitTest {
    @Test
    fun allFourContoursHaveTenPoints() {
        (1..4).forEach { tone ->
            val contour = ToneReference.getContour(tone)
            assertEquals("Тон $tone должен содержать 10 точек", 10, contour.size)
        }
    }

    @Test
    fun allContourValuesInRange() {
        (1..4).forEach { tone ->
            ToneReference.getContour(tone).forEach { value ->
                assertTrue("Значение $value тона $tone вне диапазона", value in 0f..1f)
            }
        }
    }

    @Test
    fun invalidToneThrowsException() {
        assertThrows(IllegalArgumentException::class.java) {
            ToneReference.getContour(5)
        }
    }

    private val analyzer = ToneAnalyzer()

    @Test
    fun tooFewPointsReturnsZero() {
        val result = analyzer.compare(listOf(200f, 210f, 205f), toneNumber = 1)
        assertEquals(0f, result, 0.001f)
    }

    @Test
    fun perfectTone4DescendingScoresHigh() {
        val descending = listOf(300f, 270f, 240f, 210f, 185f, 160f, 135f, 110f, 90f, 70f)
        val score = analyzer.compare(descending, toneNumber = 4)
        assertTrue("Нисходящий контур должен дать > 0.5 для тона 4, получили $score", score > 0.5f)
    }

    @Test
    fun similarityIsInZeroToOneRange() {
        val randomPoints = (1..20).map { (80 + it * 10).toFloat() }
        val result = analyzer.compare(randomPoints, toneNumber = 2)
        assertTrue("Результат $result должен быть в диапазоне 0–1", result in 0f..1f)
    }

    @Test
    fun pinyinWithToneReplacesOnlyTonedVowel() {
        val syllable = SyllableItem("妈", "māo", 1)
        val result = syllable.pinyinWithTone(4)
        assertEquals("màō заменяется неправильно", "mào", result)
    }

    @Test
    fun pinyinWithToneAllFourTones() {
        val syllable = SyllableItem("妈", "mā", 1)
        assertEquals("mā", syllable.pinyinWithTone(1))
        assertEquals("má", syllable.pinyinWithTone(2))
        assertEquals("mǎ", syllable.pinyinWithTone(3))
        assertEquals("mà", syllable.pinyinWithTone(4))
    }

    @Test
    fun pinyinWithToneNoTonedVowelReturnsSame() {
        val syllable = SyllableItem("的", "dē", 0)
        assertEquals("dē", syllable.pinyinWithTone(1))
    }
}