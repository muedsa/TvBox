package com.muedsa.tvbox.model.dandanplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BangumiTitle(
    @SerialName("language") val language: String,
    @SerialName("title") val title: String
)
