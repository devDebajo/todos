package ru.debajo.todos.auth

enum class AuthType(val code: Int) {
    NotConfigured(0), // Auth type not configured
    Weak(1), // Without pin and biometric
    Pin(2), // Only pin code
    Biometric(3); // Pin and Biometric

    companion object {
        fun fromCode(code: Int): AuthType = AuthType.entries.first { it.code == code }
    }
}
