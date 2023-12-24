package ru.debajo.todos.common

fun <T> MutableList<T>.swapRight(index: Int) {
    if (index in indices && index != lastIndex) {
        val item = removeAt(index)
        add(index + 1, item)
    }
}

fun <T> MutableList<T>.swapLeft(index: Int) {
    if (index in indices && index != 0) {
        val item = removeAt(index)
        add(index - 1, item)
    }
}
