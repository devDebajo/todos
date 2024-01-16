package ru.debajo.todos.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.debajo.todos.app.sendEmail
import ru.debajo.todos.buildconfig.BuildConfig
import ru.debajo.todos.common.formatKmp
import ru.debajo.todos.strings.R

// TODO добавить возможность сменить пин приложения
@Composable
internal fun AboutDialog(onHideClick: () -> Unit) {
    AlertDialog(
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = R.strings.appName,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(R.strings.aboutAppVersion.formatKmp(BuildConfig.APP_VERSION, BuildConfig.VERSION_NUMBER.toString()))
                Spacer(Modifier.size(4.dp))
                Text(R.strings.aboutAppDeveloper.formatKmp(BuildConfig.DEVELOPER_NAME))
                Spacer(Modifier.size(12.dp))
                TextButton({
                    onHideClick()
                    sendEmail(BuildConfig.DEVELOPER_EMAIL)
                }) {
                    Text(R.strings.aboutEmailToDeveloper)
                }
            }
        },
        confirmButton = { },
        onDismissRequest = onHideClick
    )
}
