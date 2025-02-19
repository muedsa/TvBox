package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuDanmaku(
    @SerialName("id") val id: Long = 0,
    @SerialName("content") val content: String,
    @SerialName("propertis") val properties: String,
    @SerialName("playat") val playAt: Long, //ms
)
