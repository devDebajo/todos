package ru.debajo.todos.java.utils

import java.security.SecureRandom
import kotlin.random.Random

object SecureRandomUtils {

    private val secureRandom: SecureRandom by lazy { SecureRandom() }

    fun generateIV(blockSize: Int = JvmAesHelper.blockSize): ByteArray {
        val result = ByteArray(blockSize)
        secureRandom.nextBytes(result)
        return result
    }

    fun generateSalt(size: Int = 30): String {
        val builder = StringBuilder()
        repeat(size) {
            // TODO переделать на secureRandom
            builder.append(Random.nextInt('0'.code, 'z'.code).toChar())
        }
        return builder.toString()
    }
}
