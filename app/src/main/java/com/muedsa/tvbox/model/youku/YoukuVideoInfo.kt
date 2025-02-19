package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuVideoInfo(
    @SerialName("id") val id: String,
    @SerialName("stage") val stage: String,
    @SerialName("seq") val seq: String,
    @SerialName("title") val title: String,
    @SerialName("link") val link: String,

    @SerialName("duration") val duration: String,
    @SerialName("category") val category: String,

    @SerialName("published") val published: String,
)
