package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoBarrageContentStyle(
    @SerialName("color") val color: String? = null,
)
