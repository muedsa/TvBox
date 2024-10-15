package com.muedsa.tvbox.screens.manage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun AdaptiveIconImage(drawable: Drawable, modifier: Modifier = Modifier) {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    val imageBitmap = remember { bitmap.asImageBitmap() }
    Image(
        bitmap = imageBitmap, modifier = modifier, contentDescription = null
    )
}