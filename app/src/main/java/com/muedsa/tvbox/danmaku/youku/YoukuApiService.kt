package com.muedsa.tvbox.danmaku.youku


import com.muedsa.tvbox.model.youku.YoukuRpcResp
import com.muedsa.tvbox.model.youku.YoukuSearchResp
import com.muedsa.tvbox.model.youku.YoukuVideos
import com.muedsa.tvbox.tool.ChromeUserAgent
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface YoukuApiService {

    @GET("https://search.youku.com/api/search")
    suspend fun search(
        @Query("keyword") keyword: String,
        @Query("userAgent") userAgent: String = ChromeUserAgent,
        @Query("site") site: Int = 1,
        @Query("categories") categories: Int = 0,
        @Query("ftype") fType: Int = 0,
        @Query("ob") ob: Int = 0,
        @Query("pg") pg: Int = 1,
    ): YoukuSearchResp

    @GET("v2/shows/videos.json")
    suspend fun videos(
        @Query("client_id") clientId: String = "53e6cc67237fc59a",
        @Query("package") clientPackage: String = "com.huawei.hwvplayer.youku",
        @Query("ext") ext: String = "show",
        @Query("show_id") showId: String,
    ): YoukuVideos

    @POST("https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/")
    @FormUrlEncoded
    suspend fun danmuList(
        @Field("data") data: String,
        @Query("jsv") jsv: String = "2.7.0",
        @Query("appKey") appKey: String,
        @Query("t") t: Long,
        @Query("sign") sign: String,
        @Query("api") api: String = "mopen.youku.danmu.list",
        @Query("v") v: String = "1.0",
        @Query("type") type: String = "originaljson",
        @Query("dataType") dataType: String = "jsonp",
        @Query("timeout") timeout: Int = 20000,
        @Query("jsonpIncPrefix") jsonpIncPrefix: String = "utility",
        @Header("Referer") referer: String = "https://v.youku.com"
    ): YoukuRpcResp
}