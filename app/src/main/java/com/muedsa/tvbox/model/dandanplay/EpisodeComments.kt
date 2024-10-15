package com.muedsa.tvbox.model.dandanplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EpisodeComments(
    @SerialName("count") val count: Int = 0,
    @SerialName("comments") val comments: List<EpisodeComment> = emptyList()
)
