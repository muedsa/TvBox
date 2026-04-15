package com.muedsa.tvbox.screens

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationItems {

    @Serializable
    data object Main : NavigationItems

    @Serializable
    data object PluginHome : NavigationItems

    @Serializable
    data class Detail(
        val pluginPackage: String,
        val id: String,
        val url: String,
    ) : NavigationItems

    @Serializable
    data class Player(
        val urls: List<String>,
        val httpHeadersJson: String? = null,
        val pluginPackage: String,
        val mediaId: String,
        val episodeId: String,
        val danmakuEpisodeJson: String? = null,
        val disableEpisodeProgression: Boolean,
        val enableCustomDanmakuList: Boolean,
        val enableCustomDanmakuFlow: Boolean,
        val episodeInfoJson: String,
        val skipSegments: String? = null,
    ) : NavigationItems

    @Serializable
    data object Setting : NavigationItems

    @Serializable
    data object RightSideDrawer : NavigationItems


    companion object {

        fun encodeSkipSegments(skipSegments: List<Pair<Long, Long>>?): String? {
            return skipSegments?.joinToString("|") { "${it.first},${it.second}" }
        }

        fun decodeSkipSegments(encodedString: String?): List<Pair<Long, Long>>? {
            if (encodedString.isNullOrBlank()) {
                return null
            }
            val segmentStrings = encodedString.split("|")
            return segmentStrings.mapNotNull { segment ->
                val times = segment.split(",")

                if (times.size == 2) {
                    val startTime = times[0].toLongOrNull()
                    val endTime = times[1].toLongOrNull()
                    if (startTime != null && endTime != null) {
                        Pair(startTime, endTime)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }

    }
}