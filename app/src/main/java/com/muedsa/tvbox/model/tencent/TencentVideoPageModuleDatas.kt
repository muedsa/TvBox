package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoPageModuleDatas(
    @SerialName("module_id") val moduleId: String,

    // @SerialName("module_params") val moduleParams: ,

    @SerialName("item_data_lists") val itemDataLists: TencentVideoPageModuleItemDataLists? = null,
)
