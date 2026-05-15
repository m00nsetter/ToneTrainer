package dev.moonsetter.shengcat.model

data class SyllableItem(
    val character: String,
    val pinyin: String,
    val toneNumber: Int
)

fun SyllableItem.pinyinWithTone(targetTone: Int): String {
    val vowelGroups = listOf(
        listOf('ā', 'á', 'ǎ', 'à', 'a'),
        listOf('ē', 'é', 'ě', 'è', 'e'),
        listOf('ī', 'í', 'ǐ', 'ì', 'i'),
        listOf('ō', 'ó', 'ǒ', 'ò', 'o'),
        listOf('ū', 'ú', 'ǔ', 'ù', 'u')
    )
    val index = targetTone - 1

    for (group in vowelGroups) {
        for (char in pinyin) {
            if (char in group) {
                return pinyin.replace(char, group[index])
            }
        }
    }
    return pinyin
}