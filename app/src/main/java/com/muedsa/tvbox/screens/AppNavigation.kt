package com.muedsa.tvbox.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.muedsa.compose.tv.LocalNavHostControllerProvider
import com.muedsa.compose.tv.LocalRightSideDrawerControllerProvider
import com.muedsa.compose.tv.widget.FullWidthDialogProperties
import com.muedsa.compose.tv.widget.RightSideDrawerWithNavController
import com.muedsa.compose.tv.widget.RightSideDrawerWithNavDrawerContent
import com.muedsa.tvbox.screens.detail.MediaDetailScreen
import com.muedsa.tvbox.screens.manage.PluginManageScreen
import com.muedsa.tvbox.screens.playback.PlaybackScreen
import com.muedsa.tvbox.screens.plugin.PluginScreen
import com.muedsa.tvbox.screens.setting.AppSettingScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController())  {

    val drawerController =
        RightSideDrawerWithNavController(navController, NavigationItems.RightSideDrawer.route)

    LocalNavHostControllerProvider(navController) {
        LocalRightSideDrawerControllerProvider(drawerController) {
            NavHost(
                navController = navController,
                startDestination = NavigationItems.Main.route
            ) {
                // 入口页
                composable(route = NavigationItems.Main.route) {
                    PluginManageScreen()
                }

                // 当前选择的插件主页
                composable(route = NavigationItems.PluginHome.route) {
                    PluginScreen()
                }

                // 视频详情页
                composable(
                    route = NavigationItems.Detail.route,
                    arguments = NavigationItems.Detail.args
                ) {
                    MediaDetailScreen()
                }

                // 播放页
                composable(
                    route = NavigationItems.Player.route,
                    arguments = NavigationItems.Player.args
                ) {
                    PlaybackScreen()
                }

                // 设置 Dialog
                dialog(
                    route = NavigationItems.Setting.route,
                    dialogProperties = FullWidthDialogProperties()
                ) {
                    AppSettingScreen()
                }

                // rightSideDrawer
                dialog(
                    route = NavigationItems.RightSideDrawer.route,
                    dialogProperties = FullWidthDialogProperties()
                ) {
                    RightSideDrawerWithNavDrawerContent(controller = drawerController)
                }
            }
        }
    }
}


fun buildJumpRoute(
    navItem: NavigationItems,
    pathParams: List<String>?
): String {
    var route = navItem.route
    if (navItem.args.isNotEmpty()) {
        checkNotNull(pathParams) { "route nav failure, $route#$pathParams" }
        check(pathParams.size == navItem.args.size) { "route nav failure, $route#$pathParams" }
        for (i in 0 until navItem.args.size) {
            route = route.replace("{${navItem.args[i].name}}", pathParams[i])
        }
    }
    return route
}

fun NavHostController.nav(
    navItem: NavigationItems,
    pathParams: List<String>? = null
) {
    navigate(route = buildJumpRoute(navItem, pathParams)) {
        launchSingleTop = true
    }
}