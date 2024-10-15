package com.muedsa.tvbox.model.dandanplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BangumiSearchResp<T>(
    @SerialName("hasMore") val hasMore: Boolean = false,
    @SerialName("success") val success: Boolean = false,
    @SerialName("errorCode") val errorCode: Int = -1,
    @SerialName("errorMessage") val errorMessage: String = "",
    @SerialName("animes") val animes: List<T>? = null
)
