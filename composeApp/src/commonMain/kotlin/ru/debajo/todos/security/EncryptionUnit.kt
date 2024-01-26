package ru.debajo.todos.security

data class EncryptionUnit(
    val iv: ByteArray,
    val salt: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EncryptionUnit

        if (!iv.contentEquals(other.iv)) return false
        return salt == other.salt
    }

    override fun hashCode(): Int {
        var result = iv.contentHashCode()
        result = 31 * result + salt.hashCode()
        return result
    }

    companion object {
        fun generateNew(): EncryptionUnit = EncryptionUnit(iv = randomIV(), salt = randomSalt())
    }
}

expect fun randomSalt(): String

expect fun randomIV(): ByteArray
