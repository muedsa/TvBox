package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoPageModuleItemDatas(
    @SerialName("item_id") val itemId: String,
    @SerialName("item_type") val itemType: String,
    @SerialName("item_source_type") val itemSourceType: String,
    @SerialName("item_params") val itemParams: Map<String, String>? = null,
)
