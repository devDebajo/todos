package ru.debajo.todos.ui.settings

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.common.BaseNewsLessViewModel
import ru.debajo.todos.data.storage.StorageFilesList
import ru.debajo.todos.ui.NavigatorMediator

@Stable
internal class SettingsViewModel(
    private val storageFilesList: StorageFilesList,
    private val navigatorMediator: NavigatorMediator,
) : BaseNewsLessViewModel<SettingsState>(SettingsState()) {

    override fun onLaunch() {
        screenModelScope.launch {
            val isSelectLastFile = storageFilesList.isSelectLastFile()
            updateState {
                copy(isAutoOpenLastFile = isSelectLastFile)
            }
        }
    }

    fun close() {
        screenModelScope.launch {
            navigatorMediator.back()
        }
    }

    fun onAutoOpenSwitchChanged(enabled: Boolean) {
        updateState {
            copy(isAutoOpenLastFile = enabled)
        }

        screenModelScope.launch {
            storageFilesList.setSelectLastFile(enabled)
        }
    }
}
