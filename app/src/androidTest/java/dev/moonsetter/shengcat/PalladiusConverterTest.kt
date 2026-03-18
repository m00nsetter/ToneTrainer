package dev.moonsetter.shengcat

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.moonsetter.shengcat.data.repository.pinyinToPalladius
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PalladiusConverterTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun singleSyllable() {
        val result = pinyinToPalladius("hǎo", context)
        assertEquals("хао", result)
    }

    @Test
    fun twoSyllableGreeting() {
        val result = pinyinToPalladius("yīzhǐ māo", context)
        assertEquals("ичжи мао", result)
    }

    @Test
    fun multiWordSentence() {
        val result = pinyinToPalladius("wǒ xǐhuān biānchéng", context)
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        println(result)
    }

    @Test
    fun nonPinyinPassthrough() {
        val result = pinyinToPalladius("hello world", context)
        assertEquals("hello world", result)
    }

    @Test
    fun emptyString() {
        val result = pinyinToPalladius("", context)
        assertEquals("", result)
    }

    @Test
    fun complexWord() {
        val result = pinyinToPalladius("nǚ chéngxùyuán", context)
        assertEquals("нюй чэнсюйюань", result)
        println(result)
    }

    @Test
    fun debugFailingSyllables() {
        val cases = listOf("tànxī", "nèiyàng", "kūqì", "yīqǐ", "zāogāo")
        cases.forEach {
            val result = pinyinToPalladius(it, context)
            println("$it → $result")
        }
    }
}