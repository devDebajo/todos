import ru.debajo.todos.app.JvmApplication

internal var mainIsDebug: Boolean = false
    private set

fun main(args: Array<String>) {
    mainIsDebug = args.any { it == "-d" }
    JvmApplication().run()
}
