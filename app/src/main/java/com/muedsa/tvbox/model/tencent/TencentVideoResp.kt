package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoResp<T>(
    @SerialName("ret") val ret: Int = 0,
    @SerialName("msg") val msg: String = "",
    @SerialName("data") val data: T? = null,
)
