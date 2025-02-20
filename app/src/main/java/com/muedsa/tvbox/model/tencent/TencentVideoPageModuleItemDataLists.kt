package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoPageModuleItemDataLists(
    @SerialName("item_datas") val itemDatas: List<TencentVideoPageModuleItemDatas> = emptyList(),
)
