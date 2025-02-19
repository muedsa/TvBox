package com.muedsa.tvbox.model.iqiyi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IqiyiAlbumSearchResult(
    @SerialName("code") val code: Int = -1,
    @SerialName("docinfos") val docInfos: List<IqiyiSearchDocInfo> = emptyList(),
)