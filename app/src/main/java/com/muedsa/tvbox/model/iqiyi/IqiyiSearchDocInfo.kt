package com.muedsa.tvbox.model.iqiyi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IqiyiSearchDocInfo(
    @SerialName("doc_id") val docId: String,
    @SerialName("score") val score: Float,
    @SerialName("albumDocInfo") val albumDocInfo: IqiyiAlbumDocInfo,
)
