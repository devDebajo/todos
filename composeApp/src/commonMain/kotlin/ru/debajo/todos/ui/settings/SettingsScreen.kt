package ru.debajo.todos.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.debajo.todos.common.ScreenToolbar
import ru.debajo.todos.strings.R

private val ItemHeight: Dp = 48.dp

@Composable
internal fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        ScreenToolbar(
            title = R.strings.settingsTitle,
            navigationButton = {
                IconButton({ viewModel.close() }) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.ArrowBack,
                    )
                }
            }
        )
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            SettingsSwitch(
                text = R.strings.autoOpenLastFile,
                checked = state.isAutoOpenLastFile,
                onChanged = { viewModel.onAutoOpenSwitchChanged(it) }
            )
        }
    }
}

@Composable
private fun SettingsSwitch(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.height(ItemHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
        )
        Spacer(Modifier.size(6.dp))
        Switch(
            checked = checked,
            onCheckedChange = { onChanged(it) }
        )
    }
}
