package com.muedsa.tvbox.model.dandanplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeComment(
    @SerialName("cid") val cid: Long,
    @SerialName("p") val p: String,
    @SerialName("m") val m: String
)
