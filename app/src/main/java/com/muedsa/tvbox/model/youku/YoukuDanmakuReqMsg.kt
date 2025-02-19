package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuDanmakuReqMsg(
    @SerialName("pid") val pid: Int = 0,
    @SerialName("ctype") val ctype: Int = 10004,
    @SerialName("sver") val sver: String = "3.1.0",
    @SerialName("cver") val cver: String = "v1.0",
    @SerialName("ctime") val ctime: Long,
    @SerialName("guid") val guid: String,
    @SerialName("vid") val vid: String,
    @SerialName("mat") val mat: Int,
    @SerialName("mcount") val mcount: Int = 1,
    @SerialName("type") val type: Int = 1,
)