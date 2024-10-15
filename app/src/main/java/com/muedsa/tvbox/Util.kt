package com.muedsa.tvbox

import com.muedsa.compose.tv.widget.CardType
import com.muedsa.tvbox.api.data.MediaCardType

fun MediaCardType.toCardType(): CardType = when (this) {
    MediaCardType.STANDARD -> CardType.STANDARD
    MediaCardType.COMPACT -> CardType.COMPACT
    MediaCardType.NOT_IMAGE -> CardType.STANDARD
}