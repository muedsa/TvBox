package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuRpcResp(
    //val api: String,
    @SerialName("data") val data: YoukuRpcRespData? = null,
    // ret
    // v
)