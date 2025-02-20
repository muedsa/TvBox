package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoSearchItem(
    @SerialName("videoInfo") val videoInfo: TencentVideoInfo? = null,
)
