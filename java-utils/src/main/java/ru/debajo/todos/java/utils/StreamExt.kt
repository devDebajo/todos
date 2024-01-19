package ru.debajo.todos.java.utils

import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

suspend fun OutputStream.write(content: String) {
    withContext(IO) {
        bufferedWriter().use { it.write(content) }
    }
}

suspend fun InputStream.content(): String {
    return withContext(IO) {
        bufferedReader().use { it.readText() }
    }
}
