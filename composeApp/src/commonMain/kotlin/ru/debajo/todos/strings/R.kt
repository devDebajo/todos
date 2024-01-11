package ru.debajo.todos.strings

import androidx.compose.ui.text.intl.Locale

object R {
    val strings: CommonStrings
        get() = when (Locale.current.language) {
            "ru" -> RuStrings
            else -> EnStrings
        }
}
