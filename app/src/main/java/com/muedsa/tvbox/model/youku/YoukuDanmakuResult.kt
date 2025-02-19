package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuDanmakuResult(
    @SerialName("result") val result: List<YoukuDanmaku> = emptyList(),
)
