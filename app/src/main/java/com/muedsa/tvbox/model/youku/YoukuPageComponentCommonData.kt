package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuPageComponentCommonData(
    @SerialName("showId") val showId: String,
    @SerialName("episodeTotal") val episodeTotal: Int,
    @SerialName("episodeType") val episodeType: Int? = null,
    @SerialName("realShowId") val realShowId: String,
    @SerialName("feature") val feature: String,
//    @SerialName("director") val director: String,
//    @SerialName("stripeBottom") val stripeBottom: String,
//    @SerialName("sourceImg") val sourceImg: String,
//    @SerialName("sourceName") val sourceName: String,
    @SerialName("isYouku") val isYouku: Int,
    @SerialName("hasYouku") val hasYouku: Int,
    @SerialName("ugcSupply") val ugcSupply: Int,
    @SerialName("titleDTO") val titleDTO: YoukuTitleDTO,
)
