package com.muedsa.tvbox.danmaku.tencent


import com.google.common.net.HttpHeaders
import com.muedsa.tvbox.model.tencent.TencentVideoBarrageResp
import com.muedsa.tvbox.model.tencent.TencentVideoPageData
import com.muedsa.tvbox.model.tencent.TencentVideoPageDataReq
import com.muedsa.tvbox.model.tencent.TencentVideoResp
import com.muedsa.tvbox.model.tencent.TencentVideoSearchReq
import com.muedsa.tvbox.model.tencent.TencentVideoSearchResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TencentVideoApiService {

    @POST("trpc.videosearch.mobile_search.HttpMobileRecall/MbSearchHttp")
    suspend fun search(
        @Body req: TencentVideoSearchReq,
        @Query("vplatform") videoPlatform: Int = 5,
        @Header(HttpHeaders.REFERER) referer: String = "https://m.v.qq.com/"
    ): TencentVideoResp<TencentVideoSearchResult>

    @POST("trpc.universal_backend_service.page_server_rpc.PageServer/GetPageData")
    suspend fun getPageData(
        @Body req: TencentVideoPageDataReq,
        @Query("video_appid") videoAppId: Int = 3000010,
        @Query("vplatform") videoPlatform: Int = 2,
        @Query("vversion_name") videoVersionName: String = "8.2.98",
        @Header(HttpHeaders.REFERER) referer: String = "https://v.qq.com/"
    ): TencentVideoResp<TencentVideoPageData>

    @GET("https://dm.video.qq.com/barrage/segment/{vid}/t/v1/{startMs}/{endMs}")
    suspend fun barrageSegment(
        @Path("vid") vid: String,
        @Path("startMs") startMs: Int,
        @Path("endMs") endMs: Int,
        @Header(HttpHeaders.REFERER) referer: String = "https://v.qq.com/"
    ): TencentVideoBarrageResp
}