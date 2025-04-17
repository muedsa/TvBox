package com.muedsa.tvbox.model.iqiyi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IqiyiAlbumDocInfo(
    @SerialName("albumId") val albumId: Long? = null,
    @SerialName("albumTitle") val albumTitle: String,
    @SerialName("releaseDate") val releaseDate: String,
    @SerialName("siteId") val siteId: String? = null,
    @SerialName("douban_score") val doubanScore: Float? = null,
    @SerialName("score") val score: Float? = null,
    @SerialName("channel") val channel: String,
    @SerialName("videoDocType") val videoDocType: Int,
    @SerialName("videoinfos") val videoInfos: List<IqiyiAlbumVideoInfo> = emptyList(),
)
