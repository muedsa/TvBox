package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoPageDataReq(
    @SerialName("has_cache") val hasCache: Int = 1,
    @SerialName("page_params") val pageParams: TencentVideoPageParams,
)
