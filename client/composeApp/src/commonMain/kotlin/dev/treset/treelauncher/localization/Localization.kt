package dev.treset.treelauncher.localization

import androidx.compose.runtime.*
import java.util.*

enum class Language(val locale: Locale, val strings: StringsEn, val displayName: () -> String) {
    ENGLISH(Locale.ENGLISH, StringsEn(), { Strings.language.english(SystemLanguage == ENGLISH) }),
    GERMAN(Locale.GERMAN, StringsDe(), { Strings.language.german(SystemLanguage == GERMAN) });

    override fun toString(): String {
        return this.displayName()
    }
}

val SystemLanguage = when (Locale.getDefault(Locale.Category.DISPLAY).language) {
    "de" -> Language.GERMAN
    else -> Language.ENGLISH
}

var Strings by mutableStateOf(SystemLanguage.strings)