package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoBarrageResp(
    @SerialName("barrage_list") val barrageList: List<TencentVideoBarrage> = emptyList()
)
