package ru.debajo.todos.auth

enum class AuthType(val code: Int) {
    Nothing(0),
    Pin(1),
    Biometric(2);

    companion object {
        fun fromCode(code: Int): AuthType = AuthType.entries.first { it.code == code }
    }
}
