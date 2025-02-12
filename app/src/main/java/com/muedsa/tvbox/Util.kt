package com.muedsa.tvbox

import android.Manifest
import android.os.Build
import com.muedsa.compose.tv.widget.CardType
import com.muedsa.tvbox.api.data.MediaCardType

fun MediaCardType.toCardType(): CardType = when (this) {
    MediaCardType.STANDARD -> CardType.STANDARD
    MediaCardType.COMPACT -> CardType.COMPACT
    MediaCardType.NOT_IMAGE -> CardType.COMPACT
}

val APP_PERMISSIONS: List<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // >= 30
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
        add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}