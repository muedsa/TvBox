package com.muedsa.tvbox.model.youku

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoukuSearchResp(
    @SerialName("message") val message: String,
    @SerialName("more") val more: Boolean,
    @SerialName("pageComponentList") val pageComponentList: List<YoukuPageComponent> = emptyList(),
)