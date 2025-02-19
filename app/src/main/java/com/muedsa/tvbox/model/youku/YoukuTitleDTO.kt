package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuTitleDTO(
    @SerialName("displayName") val displayName: String,
)
