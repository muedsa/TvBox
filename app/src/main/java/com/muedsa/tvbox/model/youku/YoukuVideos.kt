package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuVideos(
    @SerialName("total") val total: Int? = null,
    @SerialName("videos") val videos: List<YoukuVideoInfo>? = null
)
