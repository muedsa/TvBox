package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuDanmakuProperties(
    @SerialName("size") val size: Int,
    @SerialName("color") val color: Int,
    @SerialName("pos") val pos: Int,
)
