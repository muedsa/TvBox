package com.muedsa.tvbox.model.iqiyi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IqiyiAlbumVideoInfo(
    @SerialName("tvId") val tvId: Long? = null,
    @SerialName("itemTitle") val itemTitle: String,
    @SerialName("timeLength") val timeLength: Int? = null,
)
