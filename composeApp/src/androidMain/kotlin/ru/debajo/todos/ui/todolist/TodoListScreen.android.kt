package ru.debajo.todos.ui.todolist

import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.debajo.todos.di.getFromDi

actual fun openUrl(url: String) {
    getFromDi<Context>().startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
