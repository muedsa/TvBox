package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoSearchResult(
    @SerialName("errcode") val errCode: Int = 0,
    @SerialName("errmsg") val errMsg: String = "",
    @SerialName("normalList") val normalList: TencentVideoSearchNormalList? = null
)
