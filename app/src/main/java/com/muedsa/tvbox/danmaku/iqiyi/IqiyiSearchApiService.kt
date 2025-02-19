package com.muedsa.tvbox.danmaku.iqiyi

import com.muedsa.tvbox.model.iqiyi.IqiyiAlbumSearchResult
import com.muedsa.tvbox.model.iqiyi.IqiyiResp
import com.muedsa.util.OkHttpCacheInterceptor
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface IqiyiSearchApiService {
    @GET("o")
    suspend fun searchVideos(
        @Query("if") ifP: String = "html5",
        @Query("key") key: String,
        @Query("pageNum") pageNum: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header(OkHttpCacheInterceptor.HEADER) focusedCacheControl: String = "max-age=3600",
    ): IqiyiResp<IqiyiAlbumSearchResult>
}