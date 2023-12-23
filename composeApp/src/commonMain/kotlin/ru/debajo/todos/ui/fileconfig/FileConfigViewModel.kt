package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.ExternalFileHelper
import ru.debajo.todos.ui.fileconfig.model.FileConfigNews
import ru.debajo.todos.ui.fileconfig.model.FileConfigState

@Stable
class FileConfigViewModel(
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
    private val externalFileHelper: ExternalFileHelper,
) : StateScreenModel<FileConfigState>(FileConfigState()) {

    private val _news: MutableSharedFlow<FileConfigNews> = MutableSharedFlow()
    val news: Flow<FileConfigNews> = _news.asSharedFlow()

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
                _news.emit(FileConfigNews.NavigateToList)
            }
        }
    }

    private inline fun updateState(block: FileConfigState.() -> FileConfigState) {
        mutableState.value = mutableState.value.block()
    }
}
