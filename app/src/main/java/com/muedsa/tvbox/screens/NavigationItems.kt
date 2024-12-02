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
        val danEpisodeId: Long = -1,
        val disableEpisodeProgression: Boolean,
        val enableCustomDanmakuList: Boolean,
        val enableCustomDanmakuFlow: Boolean,
        val episodeInfoJson: String,
    ) : NavigationItems

    @Serializable
    data object Setting : NavigationItems

    @Serializable
    data object RightSideDrawer : NavigationItems
}