package com.muedsa.tvbox.danmaku.tencent

import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.danmaku.DanmakuProvider
import com.muedsa.tvbox.model.DanmakuEpisode
import com.muedsa.tvbox.model.DanmakuMedia
import com.muedsa.tvbox.model.tencent.TencentVideoBarrageContentStyle
import com.muedsa.tvbox.model.tencent.TencentVideoInfo
import com.muedsa.tvbox.model.tencent.TencentVideoPageDataReq
import com.muedsa.tvbox.model.tencent.TencentVideoPageParams
import com.muedsa.tvbox.model.tencent.TencentVideoSearchReq
import com.muedsa.tvbox.tool.LenientJson
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TencentDanmakuProvider(
    private val tencentVideoApiService: TencentVideoApiService,
) : DanmakuProvider {

    override val name: String = "腾讯"

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun searchMedia(keyword: String): List<DanmakuMedia> {
        val req = TencentVideoSearchReq(
            query = keyword,
            filterValue = "firstTabid=150",
            uuid = Uuid.random().toString().uppercase()
        )
        val resp = tencentVideoApiService.search(req)
        return resp.data?.normalList?.itemList?.mapNotNull { it.videoInfo }
            ?.mapNotNull {
                val siteInfo = it.playSites.find {
                    it.enName == "qq" && it.playSourceType == 1 && !it.episodeInfoList.isEmpty()
                }
                if (siteInfo != null) {
                    val firstEpisode = siteInfo.episodeInfoList[0]
                    DanmakuMedia(
                        provider = name,
                        mediaId = firstEpisode.cid,
                        mediaName = "${it.titleWithoutEm}(${it.typeName})",
                        publishDate = "${it.year}",
                        episodes = emptyList(),
                        extendData = LenientJson.encodeToString(it)
                    )
                } else null
            } ?: emptyList()

    }

    override suspend fun getMediaEpisodes(media: DanmakuMedia): DanmakuMedia? {
        val videoInfo = LenientJson.decodeFromString<TencentVideoInfo>(media.extendData!!)
        val firstEpisode = videoInfo.playSites.find {
            it.enName == "qq" && it.playSourceType == 1 && !it.episodeInfoList.isEmpty()
        }!!.episodeInfoList[0]
        val req = TencentVideoPageDataReq(
            pageParams = TencentVideoPageParams(
                cid = media.mediaId,
                vid = firstEpisode.id,
                pageId = "vsite_episode_list"
            )
        )
        val resp = tencentVideoApiService.getPageData(req)
        val episodes =
            resp.data?.moduleListDatas?.get(0)?.moduleDatas?.get(0)?.itemDataLists?.itemDatas?.filter {
                it.itemType == "1"
            }
        if (episodes.isNullOrEmpty()) return null

        return DanmakuMedia(
            provider = name,
            mediaId = media.mediaId,
            mediaName = media.mediaName,
            publishDate = media.publishDate,
            episodes = episodes.mapNotNull {
                val cid = it.itemParams?.get("cid")
                val vid = it.itemParams?.get("vid")
                val playTitle = it.itemParams?.get("play_title")
                val duration = it.itemParams?.get("duration")
                if (cid != null && vid != null && playTitle != null && duration != null) {
                    DanmakuEpisode(
                        provider = name,
                        mediaId = cid,
                        mediaName = media.mediaName,
                        episodeId = vid,
                        episodeName = playTitle,
                        extendData = duration,
                    )
                } else null
            },
            extendData = media.extendData
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun getEpisodeDanmakuList(episode: DanmakuEpisode): List<DanmakuItemData> {
        val duration = episode.extendData!!.toDouble() * 1000
        val list = mutableListOf<DanmakuItemData>()
        var startMs = 0
        var endMs = ONCE_OFFSET
        while (startMs < duration) {
            try {
                val resp = tencentVideoApiService.barrageSegment(
                    vid = episode.episodeId,
                    startMs = startMs,
                    endMs = endMs,
                )
                resp.barrageList.forEach {
                    var color = 0xFF_FF_FF
                    if (!it.contentStyle.isNullOrBlank()) {
                        LenientJson.decodeFromString<TencentVideoBarrageContentStyle>(it.contentStyle)
                            .color?.let {
                                color = it.hexToInt()
                            }
                    }
                    list.add(
                        DanmakuItemData(
                            danmakuId = it.id.toLong(),
                            position = it.timeOffset.toLong(),
                            content = it.content,
                            mode = DanmakuItemData.DANMAKU_MODE_ROLLING,
                            textSize = 25,
                            textColor = color,
                            score = 9,
                            danmakuStyle = DanmakuItemData.DANMAKU_STYLE_NONE
                        )
                    )
                }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
            }
            startMs = startMs + ONCE_OFFSET
            endMs = startMs + ONCE_OFFSET
            delay(100.milliseconds)
        }
        return list
    }

    companion object {
        const val ONCE_OFFSET = 2 * 60 * 1000
    }
}