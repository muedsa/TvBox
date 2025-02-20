package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoBarrage(
    @SerialName("id") val id: String,
    @SerialName("time_offset") val timeOffset: String,
    @SerialName("content_style") val contentStyle: String? = null,
    @SerialName("content") val content: String,
)
