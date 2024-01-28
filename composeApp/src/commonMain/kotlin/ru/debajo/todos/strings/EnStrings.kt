package ru.debajo.todos.strings

import ru.debajo.todos.buildconfig.BuildConfig

object EnStrings : CommonStrings {
    override val fileSessionNotificationChannelName: String = "Opened file"
    override val fileSessionNotificationChannelDescription: String = "Notification for opened file session"
    override val fileSessionNotificationMessage: String = "Opened file: %s"
    override val fileSessionNotificationChanged: String = " (Changed)"
    override val fileSessionNotificationAutoClose: String = "Will close in %s sec."
    override val fileSessionNotificationClose: String = "Close file"
    override val appName: String = BuildConfig.APP_NAME
    override val settingsTitle: String = "Settings"
    override val settingsAppVersion: String = "Version"
    override val settingsAppDeveloper: String = "Developer"
    override val settingsEmailToDeveloper: String = "Contact with developer"
    override val settingsSourceCode: String = "Source code"
    override val settingsPrivacyPolicy: String = "Privacy policy"
    override val welcome: String = "Welcome"
    override val welcomeOnboarding: String = "Before you begin, you need to configure data protection in the application"
    override val selectPreferredSecurityType: String = "Select your preferred security type"
    override val weakAuthTypeWarningDialogTitle: String = "Do not use app protection?"
    override val weakAuthTypeWarningDialogText: String = "Are you sure to disable app protection?"
    override val doNotSecureDate: String = "Do not secure data"
    override val usePin: String = "Use pin"
    override val ok: String = "Ok"
    override val delete: String = "Delete"
    override val cancel: String = "Cancel"
    override val copy: String = "Copy text"
    override val save: String = "Save"
    override val edit: String = "Edit"
    override val done: String = "Done"
    override val undone: String = "Undone"
    override val encrypt: String = "Encrypt"
    override val notEncrypt: String = "Not encrypt"
    override val noFiles: String = "No files"
    override val disable: String = "Disable"
    override val enable: String = "Enable"
    override val newFolderDialogTitle: String = "Create folder"
    override val newFolderDialogPlaceholder: String = "Folder name"
    override val renameGroupDialogTitle: String = "Rename folder"
    override val deleteTodoItemDialogTitle: String = "Delete TODO?"
    override val deleteTodoItemDialogText: String = "Are you sure to delete TODO %s?"
    override val updateItemTextDialogTitle: String = "Update TODO"
    override val updateItemTextDialogPlaceholder: String = "Text"
    override val deleteGroupDialogTitle: String = "Delete folder?"
    override val deleteGroupDialogText: String = "Are you sure to delete folder %s?"
    override val deleteGroupDialogDeleteOnlyFolder: String = "Delete only folder"
    override val deleteGroupDialogDeleteFolderWithTodos: String = "Delete folder with TODOs"
    override val fileConfigTitle: String = "Your TODO files"
    override val createFile: String = "Create file"
    override val selectFile: String = "Select file"
    override val deleteFileDialogTitle: String = "Delete file?"
    override val deleteFileDialogText: String = "Are you sure to delete file from list? These file will be stored in file system"
    override val createFileDialogText: String = "Create encrypted file? Encryption increases the security of your data"
    override val autoOpenLastFile: String = "Auto open last file"
    override val deleteFromList: String = "Delete from list"
    override val changeFilePin: String = "Change file PIN"
    override val removeFileEncryption: String = "Remove encryption"
    override val addFileEncryption: String = "Add encryption"
    override val noReadPermission: String = "No read permission"
    override val unknownFileFormat: String = "Unknown file format. Try to update app"
    override val someErrorWithFile: String = "Some error with file"
    override val pinCode: String = "PIN-code"
    override val oldPin: String = "Old PIN"
    override val newPin: String = "New PIN"
    override val confirmNewPin: String = "Confirm new PIN"
    override val pin: String = "PIN"
    override val confirmPin: String = "Confirm PIN"
    override val newPinCode: String = "New PIN-code"
    override val pinCodeFor: String = "PIN-code for %s"
    override val inputPin: String = "Input PIN"
    override val useBiometricDialogTitle: String = "Use biometric?"
    override val useBiometricDialogText: String = "Enable biometric to authentication the app?"
    override val emptyTodoList: String = "Empty"
    override val enterTodo: String = "Enter TODO"
    override val allTodosGroupName: String = "All"
    override val otherTodosGroupName: String = "Other"
    override val deleteFolder: String = "Delete folder"
    override val renameFolder: String = "Rename folder"
    override val moveFolderUp: String = "Move up"
    override val moveFolderDown: String = "Move down"
    override val moveFolderLeft: String = "Move left"
    override val moveFolderRight: String = "Move right"
    override val todoCreatedAt: String = "Created: %s"
    override val editedAt: String = "Edited: %s"
    override val biometricTitle: String = "Login in $appName"
}
