package dev.moonsetter.shengcat

import dev.moonsetter.shengcat.data.repository.hanziToPinyin
import org.junit.Assert
import org.junit.Test

class PinyinConverterTest {
    @Test
    fun word() {
        val result = hanziToPinyin("一只猫")
        Assert.assertEquals("yīzhǐ māo", result)
    }

    @Test
    fun sentence() {
        val result = hanziToPinyin("你好，我是一个软件错误。")
        println(result)
        Assert.assertNotNull(result)
        Assert.assertTrue(result.isNotEmpty())
    }

    @Test
    fun nonHanzi() {
        val result = hanziToPinyin("сковородка тефаль 123")
        println(result)
        Assert.assertEquals("сковородка тефаль 123", result)
    }

    @Test
    fun hanziAndNonHanzi() {
        val result = hanziToPinyin("你好 world")
        println(result)
        Assert.assertTrue(result.contains("world"))
        Assert.assertTrue(result.contains("ǐ") || result.contains("i"))
    }

    @Test
    fun emptyString() {
        val result = hanziToPinyin("")
        Assert.assertEquals("", result)
    }

    @Test
    fun phrases() {
        val cases = mapOf(
            "我喜欢编程" to "wǒ xǐhuān biānchéng",
            "女程序员" to "nǚ chéngxùyuán"
        )
        cases.forEach { (hanzi, expected) ->
            val result = hanziToPinyin(hanzi)
            Assert.assertEquals(expected, result)
        }
    }
}