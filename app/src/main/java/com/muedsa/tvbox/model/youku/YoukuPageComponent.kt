package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuPageComponent(
    @SerialName("commonData") val commonData: YoukuPageComponentCommonData? = null,
    @SerialName("id") val id: String,
)
