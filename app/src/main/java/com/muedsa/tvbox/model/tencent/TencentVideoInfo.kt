package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoInfo(
    @SerialName("videoType") val videoType: Int,
    @SerialName("typeName") val typeName: String,
    @SerialName("title") val title: String,
    @SerialName("year") val year: Int,
    @SerialName("playSites") val playSites: List<TencentVideoSiteInfo> = emptyList(),
) {
    @delegate:Transient
    val titleWithoutEm by lazy { title.replace("<em>", "").replace("</em>", "") }
}
