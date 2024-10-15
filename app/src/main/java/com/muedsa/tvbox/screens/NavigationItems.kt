package com.muedsa.tvbox.screens

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavigationItems(
    val route: String,
    val args: List<NamedNavArgument> = emptyList(),
) {
    data object Main : NavigationItems(route ="home")

    data object PluginHome : NavigationItems(route = "plugin_home")

    data object Detail : NavigationItems(
        route = "detail?id={id}&url={url}",
        args = listOf(
            navArgument("id") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("url") {
                type = NavType.StringType
                defaultValue = ""
            },
        )
    )

    data object Player : NavigationItems(
        route = "player?url={url}&pluginPackage={pluginPackage}&mediaId={mediaId}&episodeId={episodeId}&danEpisodeId={danEpisodeId}",
        args = listOf(
            navArgument("url") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("pluginPackage") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("mediaId") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("episodeId") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("danEpisodeId") {
                type = NavType.LongType
                defaultValue = -1
            },
        )
    )

    data object Setting : NavigationItems(route = "setting")

    data object RightSideDrawer : NavigationItems(route = "right_side_drawer")
}