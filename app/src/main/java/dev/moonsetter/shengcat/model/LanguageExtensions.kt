package dev.moonsetter.shengcat.model

import dev.moonsetter.shengcat.R

fun Language.displayName(): Int {
    return when (this) {
        Language.CHINESE -> R.string.zh_language
        Language.ENGLISH -> R.string.eng_language
        Language.RUSSIAN -> R.string.ru_language
    }
}