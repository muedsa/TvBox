package com.muedsa.tvbox.danmaku

import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.model.DanmakuEpisode
import com.muedsa.tvbox.model.DanmakuMedia

interface DanmakuProvider {

    val name: String

    suspend fun searchMedia(keyword: String): List<DanmakuMedia>

    suspend fun getMediaEpisodes(media: DanmakuMedia): DanmakuMedia?

    suspend fun getEpisodeDanmakuList(episode: DanmakuEpisode): List<DanmakuItemData>
}