package ru.debajo.todos.java.utils

import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

suspend fun OutputStream.write(content: String) {
    withContext(IO) {
        bufferedWriter().use { it.write(content) }
    }
}

fun InputStream.linesFlow(): Flow<String> {
    return flow {
        bufferedReader().use {
            for (line in it.lineSequence()) {
                emit(line)
            }
        }
    }
}
