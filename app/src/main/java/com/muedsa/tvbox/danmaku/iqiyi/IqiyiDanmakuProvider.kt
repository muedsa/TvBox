package com.muedsa.tvbox.danmaku.iqiyi

import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.danmaku.DanmakuProvider
import com.muedsa.tvbox.model.DanmakuEpisode
import com.muedsa.tvbox.model.DanmakuMedia
import com.muedsa.tvbox.model.iqiyi.IqiyiAlbumDocInfo
import com.muedsa.tvbox.tool.LenientJson
import com.muedsa.tvbox.tool.feignChrome
import com.muedsa.tvbox.tool.get
import com.muedsa.tvbox.tool.toRequestBuild
import com.muedsa.util.OkHttpCacheInterceptor
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import timber.log.Timber
import java.util.zip.InflaterInputStream
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

class IqiyiDanmakuProvider(
    val okHttpClient: OkHttpClient,
    val iqiyiSearchApiService: IqiyiSearchApiService,
) : DanmakuProvider {

    override val name: String = "爱奇艺"

    override suspend fun searchMedia(keyword: String): List<DanmakuMedia> {
        val resp = iqiyiSearchApiService.searchVideos(key = keyword)
        return resp.data?.docInfos
            ?.filter { it.score > 0.7f }
            ?.map { it.albumDocInfo }
            ?.filter {
                it.albumId != null && it.siteId == "iqiyi" && it.videoDocType == 1
                        && IQIYI_CHANNEL_LIST.contains(it.channel)
            }?.map {
                DanmakuMedia(
                    provider = name,
                    mediaId = it.albumId!!.toString(),
                    mediaName = "${it.albumTitle}(${it.channel.split(",")[0]})",
                    publishDate = it.releaseDate,
                    rating = it.doubanScore?.toString() ?: it.score?.toString(),
                    episodes = emptyList(),
                    extendData = LenientJson.encodeToString(it)
                )
            }
            ?: emptyList()
    }

    override suspend fun getMediaEpisodes(media: DanmakuMedia): DanmakuMedia? {
        if (media.extendData.isNullOrBlank()) return null
        val album = LenientJson.decodeFromString<IqiyiAlbumDocInfo>(media.extendData)
        return if(album.videoInfos.all { it.tvId != null && it.timeLength != null }) {
            DanmakuMedia(
                provider = name,
                mediaId = media.mediaId,
                mediaName = media.mediaName,
                publishDate = media.publishDate,
                rating = media.rating,
                episodes = album.videoInfos.map {
                    DanmakuEpisode(
                        provider = name,
                        mediaId = media.mediaId,
                        mediaName = media.mediaName,
                        episodeId = it.tvId?.toString() ?: media.mediaId,
                        episodeName = it.itemTitle,
                        extendData = it.timeLength?.toString()
                    )
                },
                extendData = media.extendData
            )
        } else {
            Timber.d(media.extendData)
            null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun getEpisodeDanmakuList(episode: DanmakuEpisode): List<DanmakuItemData> {
        var mat = 1
        val maxMat = ceil(episode.extendData!!.toInt() / 300.0)
        val tvId = episode.episodeId.toString()
        val s1 = tvId.substring(tvId.length - 4, tvId.length - 2)
        val s2 = tvId.substring(tvId.length - 2)
        val list = mutableListOf<DanmakuItemData>()
        while (mat < maxMat) {
            val byteStream =
                "http://cmts.iqiyi.com/bullet/${s1}/${s2}/${tvId}_300_${mat}.z".toRequestBuild()
                    .feignChrome()
                    .header(OkHttpCacheInterceptor.HEADER, "max-age=21600")
                    .get(okHttpClient = okHttpClient)
                    .body?.byteStream()
            if (byteStream == null) {
                break
            }
            val unzipBytes = InflaterInputStream(byteStream).use { it.readBytes() }
            val xml = unzipBytes.toString(Charsets.UTF_8)
            val doc = Jsoup.parse(xml, Parser.xmlParser())
            val bulletEls = doc.select("danmu >data >entry >list >bulletInfo")
            bulletEls.forEach {
                try {
                    val contentId = it.select("contentId").text().toLong()
                    val content = it.select("content").text()
                    val showTime = it.select("showTime").text().toLong()
                    val color = it.select("color").text().hexToInt()
                    val scoreLevel = it.select("scoreLevel").text().toInt()
                    val font = it.select("font").text().toInt()
                    val contentType = it.select("contentType").text().toInt()
                    list.add(
                        DanmakuItemData(
                            danmakuId = contentId.toLong(),
                            position = showTime * 1000,
                            content = content,
                            mode = when(contentType) {
                                100 -> DanmakuItemData.DANMAKU_MODE_CENTER_TOP
                                200 -> DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM
                                else -> DanmakuItemData.DANMAKU_MODE_ROLLING
                            },
                            textSize = font,
                            textColor = color,
                            score = scoreLevel,
                            danmakuStyle = DanmakuItemData.DANMAKU_STYLE_NONE
                        )
                    )
                } catch (throwable: Throwable) {
                    Timber.e(throwable, it.outerHtml())
                }
            }
            mat++
            delay(100.milliseconds)
        }
        return list
    }

    companion object {
        val IQIYI_CHANNEL_LIST = listOf(
            "电影,1",
            "电视剧,2",
            "纪录片,3",
            "动漫,4",
            "音乐,5",
            "综艺,6",
        )
    }
}