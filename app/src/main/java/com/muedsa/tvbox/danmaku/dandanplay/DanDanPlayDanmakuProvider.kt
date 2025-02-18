package com.muedsa.tvbox.danmaku.dandanplay

import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.danmaku.DanmakuProvider
import com.muedsa.tvbox.model.DanmakuEpisode
import com.muedsa.tvbox.model.DanmakuMedia
import timber.log.Timber

class DanDanPlayDanmakuProvider(
    val danDanPlayApiService: DanDanPlayApiService,
) : DanmakuProvider {

    override val name: String = "弹弹Play"

    override suspend fun searchMedia(keyword: String): List<DanmakuMedia> {
        val resp = danDanPlayApiService.searchAnime(keyword)
        return if (resp.errorCode == 0) {
            resp.animes?.map {
                DanmakuMedia(
                    provider = name,
                    mediaId = it.animeId.toString(),
                    mediaName = it.animeTitle,
                    publishDate = it.startOnlyDate,
                    rating = it.rating.toString(),
                    episodes = emptyList(),
                )
            } ?: emptyList()
        } else {
            Timber.w("danDanPlayApiService.searchAnime(${keyword}) ${resp.errorMessage}")
            emptyList()
        }
    }

    override suspend fun getMediaEpisodes(media: DanmakuMedia): DanmakuMedia? {
        val resp = danDanPlayApiService.getAnime(media.mediaId.toInt())
        return if (resp.errorCode == 0) {
            resp.bangumi?.let {
                DanmakuMedia(
                    provider = name,
                    mediaId = it.animeId.toString(),
                    mediaName = it.animeTitle,
                    publishDate = media.publishDate,
                    rating = it.rating.toString(),
                    episodes = it.episodes.map { ep ->
                        DanmakuEpisode(
                            provider = name,
                            mediaId = it.animeId.toString(),
                            mediaName = it.animeTitle,
                            episodeId = ep.episodeId.toString(),
                            episodeName = ep.episodeTitle,
                        )
                    },
                )
            }
        } else {
            Timber.d("danDanPlayApiService.getAnime(${media.mediaId}) ${resp.errorMessage}")
            null
        }
    }

    override suspend fun getEpisodeDanmakuList(episode: DanmakuEpisode): List<DanmakuItemData> {
        return danDanPlayApiService.getComment(
            episodeId = episode.episodeId.toLong(),
            from = 0,
            withRelated = true,
            chConvert = 1
        ).comments.map {
            val propArr = it.p.split(",")
            val pos = (propArr[0].toFloat() * 1000).toLong()
            val mode = if (propArr[1] == "1")
                DanmakuItemData.DANMAKU_MODE_ROLLING
            else if (propArr[1] == "4")
                DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM
            else if (propArr[1] == "5")
                DanmakuItemData.DANMAKU_MODE_CENTER_TOP
            else
                DanmakuItemData.DANMAKU_MODE_ROLLING
            val colorInt = propArr[2].toInt()
            DanmakuItemData(
                danmakuId = it.cid,
                position = pos,
                content = it.m,
                mode = mode,
                textSize = 25,
                textColor = colorInt,
                score = 9,
                danmakuStyle = DanmakuItemData.DANMAKU_STYLE_NONE
            )
        }
    }
}