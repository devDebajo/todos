package ru.debajo.todos.ui.fileconfig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.ui.pin.EnterPinDialog

@Composable
fun FileConfigScreen2(viewModel: FileConfigViewModel2) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FilesListWithPlaceholder(
            files = state.files,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(8.dp))
        Row {
            Button(onClick = { viewModel.createNewFile() }) {
                Text("Create file")
            }
            Spacer(modifier = Modifier.size(40.dp))
            Button(onClick = { viewModel.selectFile() }) {
                Text("Select file")
            }
        }
        Spacer(Modifier.size(20.dp))
    }

    CreateFileDialog(
        state = state,
        onCancel = { viewModel.hideCreateFileDialogs() },
        onConfirm = { encrypted -> viewModel.createFile(encrypted) }
    )

    val createEncryptedFileDialogState = state.createEncryptedFileDialogState
    if (createEncryptedFileDialogState != null && createEncryptedFileDialogState.visible) {
        EnterPinDialog(
            pin1 = createEncryptedFileDialogState.pin1,
            pin2 = createEncryptedFileDialogState.pin2,
            isError = createEncryptedFileDialogState.isError,
            onPin1Changed = { viewModel.onPin1Changed(it) },
            onPin2Changed = { viewModel.onPin2Changed(it) },
            onCancel = { viewModel.hideCreateFileDialogs() },
            onConfirm = { viewModel.onCreateFileWithEncryption() },
        )
    }
}

@Composable
private fun FilesListWithPlaceholder(
    files: List<StorageFile>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (files.isEmpty()) {
            Text("No files")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    count = files.size,
                    key = { files[it].absolutePath },
                    contentType = { "same" },
                    itemContent = {
                        val file = files[it]
                        FileRender(file, Modifier.fillMaxWidth())
                    }
                )
            }
        }
    }
}

@Composable
private fun FileRender(file: StorageFile, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = file.nameWithExtension
            )
            Text(
                text = file.absolutePath,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        if (file.encrypted) {
            Icon(
                modifier = Modifier.size(14.dp),
                contentDescription = null,
                imageVector = Icons.Default.Security,
            )
            Spacer(Modifier.size(10.dp))
        }
    }
}

@Composable
private fun CreateFileDialog(
    state: FileConfigState2,
    onCancel: () -> Unit,
    onConfirm: (encrypted: Boolean) -> Unit,
) {
    if (state.showCreateFileDialog) {
        AlertDialog(
            title = { Text("Create file") },
            text = { Text("Create encrypted file? Encryption increases the security of your data") },
            confirmButton = {
                TextButton(onClick = { onConfirm(true) }) {
                    Text("Encrypt")
                }
            },
            dismissButton = {
                TextButton(onClick = { onConfirm(false) }) {
                    Text("Not encrypt")
                }
            },
            onDismissRequest = onCancel
        )
    }
}
