package ru.debajo.todos.strings

object RuStrings : CommonStrings {
    override val appName: String by EnStrings::appName
    override val welcome: String = "Добро пожаловать"
    override val welcomeOnboarding: String = "Прежде чем начать нужно настроить защиту приложения"
    override val selectPreferredSecurityType: String = "Выберите предпочтительный тип защиты"
    override val weakAuthTypeWarningDialogTitle: String = "Не защищать приложение?"
    override val weakAuthTypeWarningDialogText: String = "Вы уверены, что не хотите включить защиту приложения?"
    override val doNotSecureDate: String = "Не защищать данные"
    override val usePin: String = "Использовать ПИН"
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
    override val deleteTodoItemDialogText: String = "Вы уверены, что хотите удалить TODO %S?"
    override val updateItemTextDialogTitle: String = "Обновить TODO"
    override val updateItemTextDialogPlaceholder: String = "Текст"
    override val deleteGroupDialogTitle: String = "Удалить папку?"
    override val deleteGroupDialogText: String = "Вы уверены, что хотите удалить папку %S?"
    override val deleteGroupDialogDeleteOnlyFolder: String = "Удалить только папку"
    override val deleteGroupDialogDeleteFolderWithTodos: String = "Удалить папку вместе с TODO из нее"
    override val createFile: String = "Создать файл"
    override val selectFile: String = "Выбрать файл"
    override val deleteFileDialogTitle: String = "Удалить файл?"
    override val deleteFileDialogText: String =
        "Вы уверен, что хотите удалить файл из списка? Данный файл останется будет удален только из списка в приложении, на устройстве файл все еще останется"
    override val createFileDialogText: String = "Создать зашифрованный файл? Шифрование увеличивает безопасность ваших данных"
    override val autoOpenLastFile: String = "Автоматически открывать последний файл"
    override val deleteFromList: String = "Удалить из списка"
    override val noReadPermission: String = "Нет прав для чтения файла"
    override val unknownFileFormat: String = "Неизвестный формат файла. Попробуйте обновить приложение"
    override val someErrorWithFile: String = "Ошибка при работе с файлом"
    override val pinCode: String = "ПИН-код"
    override val pin: String = "ПИН"
    override val confirmPin: String = "Подтвердите ПИН"
    override val newPinCode: String = "Новый ПИН-код"
    override val pinCodeFor: String = "ПИН-код для %S"
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
    override val todoCreatedAt: String = "Создано %S"
    override val todoEditedAt: String = "Изменено %S"
}
