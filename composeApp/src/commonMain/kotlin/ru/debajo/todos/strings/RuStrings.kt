package ru.debajo.todos.strings

object RuStrings : CommonStrings {
    override val fileSessionNotificationChannelName: String = "Открытый файл"
    override val fileSessionNotificationChannelDescription: String = "Уведомление для поддержания открытого файла"
    override val fileSessionNotificationMessage: String = "Открыт файл: %s"
    override val fileSessionNotificationChanged: String = " (Изменен)"
    override val fileSessionNotificationAutoClose: String = "Закроется через %s сек."
    override val fileSessionNotificationClose: String = "Закрыть файл"
    override val appName: String by EnStrings::appName
    override val settingsTitle: String = "Настройки"
    override val settingsAppVersion: String = "Версия"
    override val settingsAppDeveloper: String = "Разработчик"
    override val settingsEmailToDeveloper: String = "Связаться с разработчиком"
    override val settingsSourceCode: String = "Исходный код"
    override val settingsPrivacyPolicy: String = "Политика конфиденциальности"
    override val welcome: String = "Добро пожаловать"
    override val welcomeOnboarding: String = "Прежде чем начать нужно настроить защиту приложения"
    override val selectPreferredSecurityType: String = "Выберите предпочтительный тип защиты"
    override val weakAuthTypeWarningDialogTitle: String = "Не защищать приложение?"
    override val weakAuthTypeWarningDialogText: String = "Вы уверены, что не хотите включить защиту приложения?"
    override val doNotSecureDate: String = "Не защищать данные"
    override val usePin: String = "Использовать ПИН"
    override val ok: String = "Ок"
    override val delete: String = "Удалить"
    override val cancel: String = "Отмена"
    override val copy: String = "Скопировать"
    override val save: String = "Сохранить"
    override val edit: String = "Редактировать"
    override val done: String = "Выполнено"
    override val undone: String = "Не выполнено"
    override val encrypt: String = "Шифровать"
    override val notEncrypt: String = "Не шифровать"
    override val noFiles: String = "Нет файлов"
    override val disable: String = "Отключить"
    override val enable: String = "Включить"
    override val newFolderDialogTitle: String = "Создать папку"
    override val newFolderDialogPlaceholder: String = "Имя папки"
    override val renameGroupDialogTitle: String = "Переименовать папку"
    override val deleteTodoItemDialogTitle: String = "Удалить TODO?"
    override val deleteTodoItemDialogText: String = "Вы уверены, что хотите удалить TODO %s?"
    override val updateItemTextDialogTitle: String = "Обновить TODO"
    override val updateItemTextDialogPlaceholder: String = "Текст"
    override val deleteGroupDialogTitle: String = "Удалить папку?"
    override val deleteGroupDialogText: String = "Вы уверены, что хотите удалить папку %s?"
    override val deleteGroupDialogDeleteOnlyFolder: String = "Удалить только папку"
    override val deleteGroupDialogDeleteFolderWithTodos: String = "Удалить папку вместе с TODO из нее"
    override val fileConfigTitle: String = "Ваши TODO файлы"
    override val createFile: String = "Создать файл"
    override val selectFile: String = "Выбрать файл"
    override val deleteFileDialogTitle: String = "Удалить файл?"
    override val deleteFileDialogText: String =
        "Вы увереы, что хотите удалить файл из списка? Данный файл будет удален только из списка в приложении, на устройстве файл все еще останется"
    override val createFileDialogText: String = "Создать зашифрованный файл? Шифрование увеличивает безопасность ваших данных"
    override val autoOpenLastFile: String = "Автоматически открывать последний файл"
    override val deleteFromList: String = "Удалить из списка"
    override val changeFilePin: String = "Сменить ПИН файла"
    override val removeFileEncryption: String = "Снять шифрование"
    override val addFileEncryption: String = "Добавить шифрование"
    override val noReadPermission: String = "Нет прав для чтения файла"
    override val unknownFileFormat: String = "Неизвестный формат файла. Попробуйте обновить приложение"
    override val someErrorWithFile: String = "Ошибка при работе с файлом"
    override val pinCode: String = "ПИН-код"
    override val oldPin: String = "Старый ПИН"
    override val newPin: String = "Новый ПИН"
    override val confirmNewPin: String = "Подтв. новый ПИН"
    override val pin: String = "ПИН"
    override val confirmPin: String = "Подтвердите ПИН"
    override val newPinCode: String = "Новый ПИН-код"
    override val pinCodeFor: String = "ПИН-код для %s"
    override val inputPin: String = "Введите ПИН"
    override val useBiometricDialogTitle: String = "Использовать биометрию?"
    override val useBiometricDialogText: String = "Использовать биометрию для входа в приложение?"
    override val emptyTodoList: String = "Пусто"
    override val enterTodo: String = "Ваша TODO"
    override val allTodosGroupName: String = "Все"
    override val otherTodosGroupName: String = "Прочее"
    override val deleteFolder: String = "Удалить папку"
    override val renameFolder: String = "Переименовать папку"
    override val moveFolderUp: String = "Сдвинуть выше"
    override val moveFolderDown: String = "Сдвинуть ниже"
    override val moveFolderLeft: String = "Сдвинуть влево"
    override val moveFolderRight: String = "Сдвинуть право"
    override val todoCreatedAt: String = "Создано: %s"
    override val editedAt: String = "Изменено: %s"
    override val biometricTitle: String = "Вход в $appName"
}
