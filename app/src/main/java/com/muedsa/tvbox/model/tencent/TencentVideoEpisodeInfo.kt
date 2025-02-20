package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoEpisodeInfo(
    @SerialName("id") val id: String,
    @SerialName("dataType") val dataType: String,
    @SerialName("url") val url: String,
    @SerialName("title") val title: String,
    @SerialName("duration") val duration: String,
) {
    @delegate:Transient
    val cid: String by lazy {
        url.removePrefix("https://v.qq.com/x/cover/").removeSuffix(".html").split("/")[0]
    }
}
