package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoSearchReqExtraInfo(
    @SerialName("isNewMarkLabel") val isNewMarkLabel: String = "1",
)
