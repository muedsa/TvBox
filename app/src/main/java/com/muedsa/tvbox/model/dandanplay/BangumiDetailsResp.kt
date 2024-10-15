package com.muedsa.tvbox.model.dandanplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BangumiDetailsResp(
    @SerialName("success") val success: Boolean = false,
    @SerialName("errorCode") val errorCode: Int = -1,
    @SerialName("errorMessage") val errorMessage: String = "",
    @SerialName("bangumi") val bangumi: BangumiInfo? = null
)
