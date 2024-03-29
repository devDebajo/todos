package ru.debajo.todos.ui.fileconfig

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.debajo.todos.common.BlockingLoaderDialog
import ru.debajo.todos.common.PopupDialog
import ru.debajo.todos.common.PopupItem
import ru.debajo.todos.common.ScreenToolbar
import ru.debajo.todos.common.calculatePopupPosition
import ru.debajo.todos.common.contextClickable
import ru.debajo.todos.common.formatKmp
import ru.debajo.todos.strings.R
import ru.debajo.todos.ui.pin.EnterPin1Dialog
import ru.debajo.todos.ui.pin.EnterPin2Dialog
import ru.debajo.todos.ui.pin.EnterPin3Dialog

@Composable
internal fun FileConfigScreen(viewModel: FileConfigViewModel) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenToolbar(
            title = R.strings.fileConfigTitle,
            menuButton = {
                IconButton(
                    onClick = { viewModel.openSettings() },
                    content = {
                        val interactionSource = remember { MutableInteractionSource() }
                        var rotated by remember { mutableStateOf(false) }
                        val rotation by animateFloatAsState(if (rotated) 0f else -180f)
                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect { interaction ->
                                when (interaction) {
                                    is HoverInteraction.Enter -> rotated = true
                                    is HoverInteraction.Exit -> rotated = false
                                }
                            }
                        }
                        Icon(
                            modifier = Modifier.hoverable(interactionSource).rotate(rotation),
                            contentDescription = null,
                            imageVector = Icons.Rounded.Settings,
                        )
                    }
                )
            }
        )
        FilesListWithPlaceholder(
            files = state.files,
            modifier = Modifier.weight(1f),
            onPrimaryClick = { viewModel.onFilePrimaryClick(it) },
            onSecondaryClick = { file, coordinates, itemOffset ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.onFileSecondaryClick(file, coordinates.calculatePopupPosition(itemOffset))
            },
        )
        Spacer(Modifier.size(8.dp))
        Row {
            Button(onClick = { viewModel.createNewFile() }) {
                Text(R.strings.createFile)
            }
            Spacer(modifier = Modifier.size(40.dp))
            Button(onClick = { viewModel.selectFile() }) {
                Text(R.strings.selectFile)
            }
        }
        Spacer(Modifier.size(20.dp))
    }

    CreateFileDialog(
        state = state,
        onCancel = { viewModel.hideCreateFileDialogs() },
        onConfirm = { encrypted -> viewModel.createFile(encrypted) }
    )

    FileContextClickPopupMenu(
        state = state,
        onHide = { viewModel.hideFileContextPopupMenu() },
        onChangePinClick = { viewModel.onChangePinClick(ChangeFilePinState.Mode.Change) },
        onRemovePinClick = { viewModel.onChangePinClick(ChangeFilePinState.Mode.Remove) },
        onAddPinClick = { viewModel.onChangePinClick(ChangeFilePinState.Mode.AddNew) },
        onDeleteClick = { viewModel.onDeleteFileClick() },
    )

    val createEncryptedFileDialogState = state.createEncryptedFileDialogState
    if (createEncryptedFileDialogState != null && createEncryptedFileDialogState.visible) {
        EnterPin2Dialog(
            pin1 = createEncryptedFileDialogState.pin1,
            pin2 = createEncryptedFileDialogState.pin2,
            isError = createEncryptedFileDialogState.isError,
            onPin1Changed = { viewModel.onPin1Changed(it) },
            onPin2Changed = { viewModel.onPin2Changed(it) },
            onCancel = { viewModel.hideCreateFileDialogs() },
            onConfirm = { viewModel.onCreateFileWithEncryption() },
        )
    }

    val enterFilePinDialogState = state.enterFilePinDialogState
    if (enterFilePinDialogState != null && enterFilePinDialogState.visible) {
        EnterPin1Dialog(
            text = R.strings.pinCodeFor.formatKmp(enterFilePinDialogState.file.nameWithExtension),
            pin = enterFilePinDialogState.pin,
            isError = enterFilePinDialogState.isError,
            onPinChanged = { viewModel.onEnterFilePinDialogPinChanged(it) },
            onCancel = { viewModel.hideEnterFilePinDialog() },
            onConfirm = { viewModel.onConfirmEnterFilePinDialog() },
        )
    }

    DeleteFileDialog(
        state = state,
        onDelete = { viewModel.onDeleteFileConfirm() },
        onCancel = { viewModel.hideDeleteFileDialog() }
    )

    ChangeFilePinDialogGroup(
        state = state,
        onPin1Changed = { viewModel.onChangeFilePin1Changed(it) },
        onPin2Changed = { viewModel.onChangeFilePin2Changed(it) },
        onPin3Changed = { viewModel.onChangeFilePin3Changed(it) },
        onCancel = { viewModel.hideChangeFilePinDialog() },
        onConfirm = { viewModel.confirmChangeFilePinDialog() },
    )

    SnackbarHost(viewModel)
    BlockingLoaderDialog(state.showBlockingLoading)
}

@Composable
private fun FileContextClickPopupMenu(
    state: FileConfigState,
    onHide: () -> Unit,
    onChangePinClick: () -> Unit,
    onRemovePinClick: () -> Unit,
    onAddPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val filePopupMenuState = state.filePopupMenuState
    PopupDialog(
        visible = filePopupMenuState?.visible == true,
        position = filePopupMenuState?.position ?: IntOffset.Zero,
        onHide = onHide
    ) {
        val file = filePopupMenuState?.file
        if (file != null) {
            if (file.encrypted) {
                PopupItem(
                    modifier = Modifier.widthIn(min = 100.dp),
                    text = R.strings.changeFilePin,
                    onClick = onChangePinClick,
                )

                PopupItem(
                    modifier = Modifier.widthIn(min = 100.dp),
                    text = R.strings.removeFileEncryption,
                    onClick = onRemovePinClick,
                )
            } else {
                PopupItem(
                    modifier = Modifier.widthIn(min = 100.dp),
                    text = R.strings.addFileEncryption,
                    onClick = onAddPinClick,
                )
            }
        }

        PopupItem(
            modifier = Modifier.widthIn(min = 100.dp),
            text = R.strings.deleteFromList,
            onClick = onDeleteClick,
        )
    }
}

@Composable
private fun FilesListWithPlaceholder(
    files: List<UiStorageFile>?,
    onPrimaryClick: (UiStorageFile) -> Unit,
    onSecondaryClick: (UiStorageFile, LayoutCoordinates, Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            files == null -> Unit
            files.isEmpty() -> Text(R.strings.noFiles)
            else -> {
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
                            FileRender(
                                file = file,
                                onPrimaryClick = onPrimaryClick,
                                onSecondaryClick = onSecondaryClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileRender(
    file: UiStorageFile,
    onPrimaryClick: (UiStorageFile) -> Unit,
    onSecondaryClick: (UiStorageFile, LayoutCoordinates, Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    var position by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .contextClickable(
                onPrimaryClick = { onPrimaryClick(file) },
                onSecondaryClick = { itemOffset -> position?.let { onSecondaryClick(file, it, itemOffset) } },
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .onGloballyPositioned { position = it }
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = file.nameWithExtension
            )
            Text(
                text = R.strings.editedAt.formatKmp(file.editedFormatted),
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(8.dp))
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
    state: FileConfigState,
    onCancel: () -> Unit,
    onConfirm: (encrypted: Boolean) -> Unit,
) {
    if (state.showCreateFileDialog) {
        AlertDialog(
            title = { Text(R.strings.createFileDialogTitle) },
            text = { Text(R.strings.createFileDialogText) },
            confirmButton = {
                TextButton(onClick = { onConfirm(true) }) {
                    Text(R.strings.encrypt)
                }
            },
            dismissButton = {
                TextButton(onClick = { onConfirm(false) }) {
                    Text(R.strings.notEncrypt)
                }
            },
            onDismissRequest = onCancel
        )
    }
}

@Composable
private fun SnackbarHost(viewModel: FileConfigViewModel) {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, hostState) {
        viewModel.news.collect { news ->
            if (news is FileConfigNews.Toast) {
                hostState.showSnackbar(message = news.text)
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DeleteFileDialog(
    state: FileConfigState,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    if (state.filePopupMenuState?.showDeleteDialog == true) {
        AlertDialog(
            title = { Text(R.strings.deleteFileDialogTitle) },
            text = { Text(R.strings.deleteFileDialogText) },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text(R.strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(R.strings.cancel)
                }
            },
            onDismissRequest = onCancel
        )
    }
}

@Composable
private fun ChangeFilePinDialogGroup(
    state: FileConfigState,
    onPin1Changed: (TextFieldValue) -> Unit,
    onPin2Changed: (TextFieldValue) -> Unit,
    onPin3Changed: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val changeFilePinState = state.filePopupMenuState?.changeFilePinState ?: return
    when (changeFilePinState.mode) {
        ChangeFilePinState.Mode.AddNew -> EnterPin2Dialog(
            pin1 = changeFilePinState.pin2,
            pin2 = changeFilePinState.pin3,
            isError = changeFilePinState.isError,
            onPin1Changed = onPin2Changed,
            onPin2Changed = onPin3Changed,
            onCancel = onCancel,
            onConfirm = onConfirm,
        )

        ChangeFilePinState.Mode.Remove -> EnterPin1Dialog(
            pin = changeFilePinState.pin1,
            isError = changeFilePinState.isError,
            onPinChanged = onPin1Changed,
            onCancel = onCancel,
            onConfirm = onConfirm,
        )

        ChangeFilePinState.Mode.Change -> EnterPin3Dialog(
            pin1 = changeFilePinState.pin1,
            pin2 = changeFilePinState.pin2,
            pin3 = changeFilePinState.pin3,
            isError = changeFilePinState.isError,
            onPin1Changed = onPin1Changed,
            onPin2Changed = onPin2Changed,
            onPin3Changed = onPin3Changed,
            onCancel = onCancel,
            onConfirm = onConfirm,
        )
    }
}
