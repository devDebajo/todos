package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.common.BaseNewsLessViewModel
import ru.debajo.todos.security.SecuredPreferences

@Stable
class FileConfigViewModel2(
    private val securedPreferences: SecuredPreferences,
) : BaseNewsLessViewModel<FileConfigState2>(FileConfigState2()) {

    override fun onLaunch() {
        screenModelScope.launch {

        }
    }
}
