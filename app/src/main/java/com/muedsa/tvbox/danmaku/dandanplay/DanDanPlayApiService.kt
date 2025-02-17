package com.muedsa.tvbox.danmaku.dandanplay

import com.muedsa.tvbox.model.dandanplay.BangumiDetailsResp
import com.muedsa.tvbox.model.dandanplay.BangumiSearch
import com.muedsa.tvbox.model.dandanplay.BangumiSearchResp
import com.muedsa.tvbox.model.dandanplay.EpisodeComments
import com.muedsa.util.OkHttpCacheInterceptor
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface DanDanPlayApiService {

    @GET("v2/search/anime")
    suspend fun searchAnime(
        @Query("keyword") keyword: String,
        @Query("type") type: String = "",
        @Header(OkHttpCacheInterceptor.HEADER) focusedCacheControl: String = "max-age=3600",
    ): BangumiSearchResp<BangumiSearch>

    @GET("v2/bangumi/{animeId}")
    suspend fun getAnime(
        @Path("animeId") animeId: Int,
        @Header(OkHttpCacheInterceptor.HEADER) focusedCacheControl: String = "max-age=21600",
    ): BangumiDetailsResp

    @GET("v2/comment/{episodeId}")
    suspend fun getComment(
        @Path("episodeId") episodeId: Long,
        @Query("from") from: Int = 0,
        @Query("withRelated") withRelated: Boolean = false,
        @Query("chConvert") chConvert: Int = 0,
        @Header(OkHttpCacheInterceptor.HEADER) focusedCacheControl: String = "max-age=21600",
    ): EpisodeComments

}