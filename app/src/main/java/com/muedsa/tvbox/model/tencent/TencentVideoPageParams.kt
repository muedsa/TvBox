package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoPageParams(
    @SerialName("cid") val cid: String,
    @SerialName("vid") val vid: String,
    @SerialName("detail_page_type") val detailPageType: Int = 1,
    @SerialName("id_type") val idType: Int = 1,
    @SerialName("lid") val lid: String = "",
    @SerialName("page_context") val pageContext: String = "",
    @SerialName("page_id") val pageId: String,
    @SerialName("page_num") val pageNum: String = "",
    @SerialName("page_size") val pageSize: String = "",
    @SerialName("page_type") val pageType: String = "detail_operation",
    @SerialName("req_from") val reqFrom: String = "web_vsite",
)
