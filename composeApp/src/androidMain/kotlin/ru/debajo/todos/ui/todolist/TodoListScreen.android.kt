package ru.debajo.todos.ui.todolist

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import ru.debajo.todos.common.toIntOffset
import ru.debajo.todos.di.getFromDi

actual fun LayoutCoordinates.calculatePopupPosition(itemOffset: Offset): IntOffset {
    return positionInRoot().toIntOffset() + itemOffset.toIntOffset()
}

actual fun openUrl(url: String) {
    getFromDi<Context>().startActivity(
        Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
