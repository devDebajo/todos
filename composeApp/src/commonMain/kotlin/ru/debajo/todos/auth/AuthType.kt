package ru.debajo.todos.auth

enum class AuthType(val code: Int, val secured: Boolean) {
    NotConfigured(0, false), // Auth type not configured
    Weak(1, false), // Without pin and biometric
    Pin(2, true), // Only pin code
    Biometric(3, true); // Pin and Biometric

    companion object {
        fun fromCode(code: Int): AuthType = AuthType.entries.first { it.code == code }
    }
}
