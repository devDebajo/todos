package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.ExternalFileHelper
import ru.debajo.todos.ui.fileconfig.model.FileConfigState

@Stable
class FileConfigViewModel(
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
    private val externalFileHelper: ExternalFileHelper,
) : StateScreenModel<FileConfigState>(FileConfigState()) {

    fun init() {
        screenModelScope.launch {
            externalFileHelper.fileUri.collect {
                updateState {
                    copy(currentFileUri = it)
                }
            }
        }
    }

    fun createNewFile() {
        externalFileHelper.create()
    }

    fun selectFile() {
        externalFileHelper.selectFile()
    }

    fun openList() {
        screenModelScope.launch {
            updateState {
                copy(loading = true)
            }
            runCatchingAsync {
                databaseSnapshotSaver.load()
            }.onFailure {
                it.printStackTrace()
                updateState { copy(loading = false) }
            }.onSuccess {
                updateState { copy(loading = false) }
                //navigator.navigate(AppScreen.List)
            }
        }
    }

    private inline fun updateState(block: FileConfigState.() -> FileConfigState) {
        mutableState.value = mutableState.value.block()
    }
}
