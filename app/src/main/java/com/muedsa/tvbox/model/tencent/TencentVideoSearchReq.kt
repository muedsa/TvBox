package com.muedsa.tvbox.model.tencent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TencentVideoSearchReq(
    @SerialName("version") val version: String = "24060601",
    @SerialName("clientType") val clientType: Int = 1,
    @SerialName("filterValue") val filterValue: String = "",
    @SerialName("uuid") val uuid: String,
    @SerialName("retry") val retry: Int = 0,
    @SerialName("query") val query: String,
    @SerialName("pagenum") val pageNum: Int = 0,
    @SerialName("pagesize") val pageSize: Int = 14,
    @SerialName("queryFrom") val queryFrom: Int = 0,
    @SerialName("searchDatakey") val searchDataKey: String = "",
    @SerialName("transInfo") val transInfo: String = "",
    @SerialName("isneedQc") val isneedQc: Boolean = true,
    @SerialName("preQid") val preQid: String = "",
    @SerialName("adClientInfo") val adClientInfo: String = "",
    @SerialName("extraInfo") val extraInfo: TencentVideoSearchReqExtraInfo = TencentVideoSearchReqExtraInfo(),
)
