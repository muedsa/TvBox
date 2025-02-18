package com.muedsa.tvbox.danmaku

import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.model.DanmakuEpisode
import com.muedsa.tvbox.model.DanmakuMedia

class DanmakuService {

    private val providers: MutableMap<String, DanmakuProvider> = mutableMapOf()

    fun register(provider: DanmakuProvider) {
        providers[provider.name] = provider
    }

    fun getProviders(): Set<String> {
        return providers.keys
    }

    suspend fun searchMedia(provider: String, keyword: String): List<DanmakuMedia> {
        return providers[provider]?.searchMedia(keyword) ?: emptyList()
    }

    suspend fun getMediaEpisodes(media: DanmakuMedia): DanmakuMedia? {
        return providers[media.provider]?.getMediaEpisodes(media)
    }

    suspend fun getEpisodeDanmakuList(episode: DanmakuEpisode): List<DanmakuItemData> {
        return providers[episode.provider]?.getEpisodeDanmakuList(episode) ?: emptyList()
    }
}