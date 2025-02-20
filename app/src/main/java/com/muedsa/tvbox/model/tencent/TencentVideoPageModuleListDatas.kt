package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoPageModuleListDatas(
    @SerialName("module_datas") val moduleDatas: List<TencentVideoPageModuleDatas>,
)
