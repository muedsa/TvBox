package com.muedsa.compose.tv.widget

import android.view.KeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent

fun Modifier.onDpadKeyEvents(
    onUp: () -> Boolean = { false },
    onDown: () -> Boolean = { false },
    onLeft: () -> Boolean = { false },
    onRight: () -> Boolean = { false },
    onCenter: () -> Boolean = { false }
): Modifier = this then Modifier.onPreviewKeyEvent {
    var handle = false
    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
        handle = when (it.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> onUp()
            KeyEvent.KEYCODE_DPAD_DOWN -> onDown()
            KeyEvent.KEYCODE_DPAD_LEFT -> onLeft()
            KeyEvent.KEYCODE_DPAD_RIGHT -> onRight()
            KeyEvent.KEYCODE_DPAD_CENTER -> onCenter()
            else -> false
        }
    }
    return@onPreviewKeyEvent handle
}