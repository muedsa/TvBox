package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoPageData(
    @SerialName("module_list_datas") val moduleListDatas: List<TencentVideoPageModuleListDatas>? = null,
)
