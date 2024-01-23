package ru.debajo.todos.app

import java.util.Locale

internal val currentOS: OS by lazy { OS.getCurrent() }

internal enum class OS {
    WINDOWS,
    MAC,
    LINUX;

    companion object {
        fun getCurrent(): OS {
            val name = System.getProperty("os.name").lowercase(Locale.getDefault())
            return when {
                name.contains("win") -> OS.WINDOWS
                name.contains("mac") -> OS.MAC
                else -> LINUX
            }
        }
    }
}
