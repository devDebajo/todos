package ru.debajo.todos.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.debajo.todos.di.getFromDi

internal actual fun openUrl(url: String) {
    getFromDi<Context>().startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

internal actual fun sendEmail(email: String) {
    getFromDi<Context>().startActivity(
        Intent(Intent.ACTION_SENDTO)
            .setData(Uri.parse("mailto:$email"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
