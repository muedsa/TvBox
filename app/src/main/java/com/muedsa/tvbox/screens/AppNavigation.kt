package com.muedsa.tvbox.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muedsa.compose.tv.LocalFocusTransferredOnLaunchProvider
import com.muedsa.compose.tv.LocalLastFocusedItemPerDestinationProvider
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
        RightSideDrawerWithNavController(navController, NavigationItems.RightSideDrawer)

    LocalNavHostControllerProvider(navController) {
        LocalRightSideDrawerControllerProvider(drawerController) {
            LocalLastFocusedItemPerDestinationProvider {
                NavHost(
                    navController = navController,
                    startDestination = NavigationItems.Main
                ) {
                    // 入口页
                    composable<NavigationItems.Main> {
                        LocalFocusTransferredOnLaunchProvider {
                            PluginManageScreen()
                        }
                    }

                    // 当前选择的插件主页
                    composable<NavigationItems.PluginHome> {
                        LocalFocusTransferredOnLaunchProvider {
                            PluginScreen()
                        }
                    }

                    // 视频详情页
                    composable<NavigationItems.Detail> {
                        LocalFocusTransferredOnLaunchProvider {
                            val navItem = it.toRoute<NavigationItems.Detail>()
                            MediaDetailScreen(navItem = navItem)
                        }
                    }

                    // 播放页
                    composable<NavigationItems.Player> {
                        val navItem = it.toRoute<NavigationItems.Player>()
                        PlaybackScreen(navItem = navItem)
                    }

                    // 设置 Dialog
                    dialog<NavigationItems.Setting>(dialogProperties = FullWidthDialogProperties()) {
                        AppSettingScreen()
                    }

                    // rightSideDrawer
                    dialog<NavigationItems.RightSideDrawer>(dialogProperties = FullWidthDialogProperties()) {
                        RightSideDrawerWithNavDrawerContent(controller = drawerController)
                    }
                }
            }

        }
    }
}

fun NavHostController.nav(navItem: NavigationItems) {
    navigate(navItem) {
        launchSingleTop = true
    }
}