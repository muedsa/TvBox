package com.muedsa.tvbox.model.dandanplay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BangumiEpisode(
    @SerialName("episodeId") val episodeId: Long,
    @SerialName("episodeTitle") val episodeTitle: String,
    @SerialName("episodeNumber") val episodeNumber: String,
    @SerialName("lastWatched") val lastWatched: String? = null,
    @SerialName("airDate") val airDate: String? = null,
)