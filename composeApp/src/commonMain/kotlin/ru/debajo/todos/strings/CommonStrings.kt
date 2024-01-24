package ru.debajo.todos.strings

interface CommonStrings {
    val fileSessionNotificationChannelName: String
    val fileSessionNotificationChannelDescription: String
    val fileSessionNotificationMessage: String
    val fileSessionNotificationAutoClose: String
    val fileSessionNotificationClose: String

    val appName: String

    val aboutTitle: String
    val aboutAppVersion: String
    val aboutAppDeveloper: String
    val aboutEmailToDeveloper: String

    val welcome: String
    val welcomeOnboarding: String
    val selectPreferredSecurityType: String
    val weakAuthTypeWarningDialogTitle: String
    val weakAuthTypeWarningDialogText: String
    val doNotSecureDate: String
    val usePin: String

    val ok: String
    val delete: String
    val cancel: String
    val copy: String
    val save: String
    val edit: String
    val done: String
    val undone: String
    val encrypt: String
    val notEncrypt: String
    val noFiles: String
    val disable: String
    val enable: String

    val newFolderDialogTitle: String
    val newFolderDialogPlaceholder: String
    val renameGroupDialogTitle: String
    val renameGroupDialogPlaceholder: String
        get() = newFolderDialogPlaceholder
    val deleteTodoItemDialogTitle: String
    val deleteTodoItemDialogText: String
    val updateItemTextDialogTitle: String
    val updateItemTextDialogPlaceholder: String
    val deleteGroupDialogTitle: String
    val deleteGroupDialogText: String
    val deleteGroupDialogDeleteOnlyFolder: String
    val deleteGroupDialogDeleteFolderWithTodos: String

    val fileConfigTitle: String
    val createFile: String
    val selectFile: String
    val deleteFileDialogTitle: String
    val deleteFileDialogText: String
    val createFileDialogTitle: String
        get() = createFile
    val createFileDialogText: String
    val autoOpenLastFile: String
    val deleteFromList: String
    val changeFilePin: String
    val removeFileEncryption: String
    val addFileEncryption: String
    val noReadPermission: String
    val unknownFileFormat: String
    val someErrorWithFile: String

    val pinCode: String
    val oldPin: String
    val newPin: String
    val confirmNewPin: String
    val pin: String
    val confirmPin: String
    val newPinCode: String
    val pinCodeFor: String
    val inputPin: String

    val useBiometricDialogTitle: String
    val useBiometricDialogText: String

    val emptyTodoList: String
    val doneDivider: String
        get() = done
    val enterTodo: String
    val allTodosGroupName: String
    val otherTodosGroupName: String
    val deleteFolder: String
    val renameFolder: String
    val moveFolderUp: String
    val moveFolderDown: String
    val moveFolderLeft: String
    val moveFolderRight: String
    val todoCreatedAt: String
    val editedAt: String

    val biometricTitle: String
}
