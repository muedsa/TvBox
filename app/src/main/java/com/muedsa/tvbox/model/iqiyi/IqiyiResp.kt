package com.muedsa.tvbox.model.iqiyi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IqiyiResp<T>(
    @SerialName("code") val code: String = "",
    @SerialName("data") val data: T? = null,
)