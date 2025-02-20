package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoSiteInfo(
    @SerialName("uiType") val uiType: Int,
    @SerialName("showName") val showName: String,
    @SerialName("enName") val enName: String,
    @SerialName("playsourceType") val playSourceType: Int,
    @SerialName("isDanger") val isDanger: Int,
    @SerialName("totalEpisode") val totalEpisode: Int,
    @SerialName("episodeInfoList") val episodeInfoList: List<TencentVideoEpisodeInfo> = emptyList(),
)
