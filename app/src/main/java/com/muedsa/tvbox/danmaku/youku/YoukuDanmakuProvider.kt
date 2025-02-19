package com.muedsa.tvbox.danmaku.youku

import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.danmaku.DanmakuProvider
import com.muedsa.tvbox.model.DanmakuEpisode
import com.muedsa.tvbox.model.DanmakuMedia
import com.muedsa.tvbox.model.youku.YoukuDanmakuProperties
import com.muedsa.tvbox.model.youku.YoukuDanmakuReqMsg
import com.muedsa.tvbox.model.youku.YoukuDanmakuReqSignedMsg
import com.muedsa.tvbox.model.youku.YoukuDanmakuResp
import com.muedsa.tvbox.tool.LenientJson
import com.muedsa.tvbox.tool.SharedCookieSaver
import com.muedsa.tvbox.tool.encodeBase64
import com.muedsa.tvbox.tool.feignChrome
import com.muedsa.tvbox.tool.get
import com.muedsa.tvbox.tool.md5
import com.muedsa.tvbox.tool.toRequestBuild
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

class YoukuDanmakuProvider(
    private val cookieSaver: SharedCookieSaver,
    private val okHttpClient: OkHttpClient,
    private val youkuApiService: YoukuApiService,
) : DanmakuProvider {

    init {
        cookieSaver.load().filter { it.domain == "youku.com" || it.domain == "mmstat.com" }
            .forEach { cookieSaver.remove(it) }
    }

    override val name: String = "优酷"

    var cna: String? = null
    var token: String? = null
    var tokenEnc: String? = null

    override suspend fun searchMedia(keyword: String): List<DanmakuMedia> {
        val resp = youkuApiService.search(keyword = keyword)
        return resp.pageComponentList
            .mapNotNull { it.commonData }
            .filter { it.isYouku == 1 && it.hasYouku == 1 && it.ugcSupply == 0 }
            .map {
                val feats = it.feature.split(" ")
                DanmakuMedia(
                    provider = name,
                    mediaId = it.showId,
                    mediaName = it.titleDTO.displayName,
                    publishDate = if (feats.size > 2) "${feats[0]} ${feats[1]}" else feats[0],
                    episodes = emptyList(),
                )
            }
    }

    override suspend fun getMediaEpisodes(media: DanmakuMedia): DanmakuMedia? {
        val resp = youkuApiService.videos(showId = media.mediaId)
        return resp.videos?.let {
            DanmakuMedia(
                provider = name,
                mediaId = media.mediaId,
                mediaName = media.mediaName,
                publishDate = media.publishDate,
                episodes = it.map {
                        DanmakuEpisode(
                            provider = name,
                            mediaId = media.mediaId,
                            mediaName = media.mediaName,
                            episodeId = it.id,
                            episodeName = it.title,
                            extendData = it.duration
                        )
                    }
            )
        }
    }

    override suspend fun getEpisodeDanmakuList(episode: DanmakuEpisode): List<DanmakuItemData> {
        ensureToken()
        var mat = 0
        val maxMat = ceil(episode.extendData!!.toDouble() / 60).toInt()
        val list = mutableListOf<DanmakuItemData>()
        while (mat < maxMat) {
            val t = System.currentTimeMillis()
            val msg = YoukuDanmakuReqMsg(
                ctime = t,
                guid = cna!!,
                vid = episode.episodeId,
                mat = mat
            )
            val msgJson = LenientJson.encodeToString(msg)
            val msgEnc = msgJson.toByteArray(Charsets.UTF_8).encodeBase64()
            val msgSign = generateMsgSign(msgEnc)
            val signedMsg = YoukuDanmakuReqSignedMsg.from(msg = msg, msgEnc = msgEnc, msgSign = msgSign)
            val appKey = "24679788"
            val data = LenientJson.encodeToString(signedMsg)
            val resp = youkuApiService.danmuList(
                data = data,
                appKey = appKey,
                t = t,
                sign = generateTokenSign("$t", appKey, data)
            )
            if (!resp.data?.result.isNullOrBlank()) {
                val danmakuResp = LenientJson.decodeFromString<YoukuDanmakuResp>(resp.data.result)
                danmakuResp.data?.result?.forEach {
                    val props = LenientJson.decodeFromString<YoukuDanmakuProperties>(it.properties)
                    list.add(
                        DanmakuItemData(
                            danmakuId = it.id,
                            position = it.playAt,
                            content = it.content,
                            mode = DanmakuItemData.DANMAKU_MODE_ROLLING,
                            textSize = 25,
                            textColor = props.color,
                            score = 9,
                            danmakuStyle = DanmakuItemData.DANMAKU_STYLE_NONE
                        )
                    )
                }
            }
            mat++
            delay(100.milliseconds)
        }
       return list
    }

    fun ensureToken() {
        var cookies = cookieSaver.load()
        cna = cookies.find { it.name == "cna" && (it.domain == "youku.com" || it.domain == "mmstat.com") }?.value
        if (cna.isNullOrBlank()) {
            "https://log.mmstat.com/eg.js"
                .toRequestBuild()
                .feignChrome(referer = "https://youku.com/")
                .get(okHttpClient)
            cookies = cookieSaver.load()
            cna = cookies.find { it.name == "cna" && (it.domain == "youku.com" || it.domain == "mmstat.com") }?.value
        }

        token = cookies.find { it.name == "_m_h5_tk" && it.domain == "youku.com" }?.value
        tokenEnc = cookies.find { it.name == "_m_h5_tk_enc" && it.domain == "youku.com" }?.value
        if (token.isNullOrBlank() || tokenEnc.isNullOrBlank()) {
            "https://acs.youku.com/h5/mtop.com.youku.aplatform.weakget/1.0/?jsv=2.5.1&appKey=24679788"
                .toRequestBuild()
                .feignChrome(referer = "https://youku.com/")
                .get(okHttpClient)
            cookies = cookieSaver.load()
            token = cookies.find { it.name == "_m_h5_tk" && it.domain == "youku.com" }?.value
            tokenEnc = cookies.find { it.name == "_m_h5_tk_enc" && it.domain == "youku.com" }?.value
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun generateMsgSign(msgEnc: String): String =
        "${msgEnc}MkmC9SoIw6xCkSKHhJ7b5D2r51kBiREr".md5().toHexString()

    @OptIn(ExperimentalStdlibApi::class)
    fun generateTokenSign(t: String, appKey: String, data: String): String =
        listOf(token!!.substring(0, 32), t, appKey, data).joinToString("&").md5().toHexString()
}