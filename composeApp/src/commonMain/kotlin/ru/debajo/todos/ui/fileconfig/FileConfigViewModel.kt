package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import ru.debajo.todos.ui.fileconfig.model.FileConfigState

@Stable
class FileConfigViewModel : StateScreenModel<FileConfigState>(FileConfigState()) {

    fun selectFile() {

    }

    fun createNewFile() {

    }

    fun openList() {

    }

    private inline fun updateState(block: FileConfigState.() -> FileConfigState) {
        mutableState.value = mutableState.value.block()
    }
}
