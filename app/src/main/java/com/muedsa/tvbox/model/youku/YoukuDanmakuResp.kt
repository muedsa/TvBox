package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuDanmakuResp(
    // code
    // cost
    @SerialName("data") val data: YoukuDanmakuResult? = null,
)
