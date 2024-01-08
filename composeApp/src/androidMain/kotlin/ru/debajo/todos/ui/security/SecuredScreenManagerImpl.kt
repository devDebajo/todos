package ru.debajo.todos.ui.security

import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import ru.debajo.todos.ActivityResultLaunchers

internal class SecuredScreenManagerImpl(
    private val activityResultLaunchersProvider: () -> ActivityResultLaunchers,
) : SecuredScreenManager {

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var secured: Boolean = false
    private val window: Window
        get() = activityResultLaunchersProvider().activity.window

    fun onCreate() {
        setScreenSecured(secured)
    }

    override fun setScreenSecured(secured: Boolean) {
        this.secured = secured

        handler.post {
            if (secured) {
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}
