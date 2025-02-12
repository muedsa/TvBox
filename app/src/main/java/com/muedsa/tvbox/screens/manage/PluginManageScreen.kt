package com.muedsa.tvbox.screens.manage

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalTextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import androidx.tv.material3.WideClassicCard
import com.muedsa.compose.tv.conditional
import com.muedsa.compose.tv.focusOnMount
import com.muedsa.compose.tv.theme.ScreenPaddingLeft
import com.muedsa.compose.tv.useLocalLastFocusedItemPerDestination
import com.muedsa.compose.tv.useLocalNavHostController
import com.muedsa.compose.tv.useLocalRightSideDrawerController
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ErrorScreen
import com.muedsa.compose.tv.widget.LoadingScreen
import com.muedsa.tvbox.APP_PERMISSIONS
import com.muedsa.tvbox.plugin.LoadedPlugins
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.screens.SPECIAL_DESTINATION_PLUGIN_HOME
import com.muedsa.tvbox.screens.nav
import com.muedsa.tvbox.screens.plugin.home.HOME_FIRST_ROW_FOCUS_ON_MOUNT_KEY
import com.muedsa.util.AppUtil


@Composable
fun PluginManageScreen(
    pluginManageScreenViewModel: PluginManageScreenViewModel = hiltViewModel()
) {
    val uiState by pluginManageScreenViewModel.uiState.collectAsState()
    val toastController = useLocalToastMsgBoxController()

    when (val s = uiState) {
        is PluginManageUiState.Loading -> LoadingScreen()

        is PluginManageUiState.Error -> ErrorScreen(onError = {
            toastController.error(s.error)
        }, onRefresh = { pluginManageScreenViewModel.refreshPluginInfoList() })

        is PluginManageUiState.Ready -> PluginManage(
            pluginManageScreenViewModel = pluginManageScreenViewModel,
            loadedPlugins = s.loadedPlugins,
            apiVersion = s.apiVersion
        )
    }
}

@Composable
fun PluginManage(
    pluginManageScreenViewModel: PluginManageScreenViewModel = hiltViewModel(),
    loadedPlugins: LoadedPlugins,
    apiVersion : Int,
) {
    val navController = useLocalNavHostController()
    val lastFocusedItemPerDestination = useLocalLastFocusedItemPerDestination()
    val toastController = useLocalToastMsgBoxController()
    val drawerController = useLocalRightSideDrawerController()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(pluginManageScreenViewModel.hasPermissions()) }
    val permissionRequester = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasPermission = it.values.all { it }
    }

    var deleteMode by remember { mutableStateOf(false) }

    BackHandler(enabled = deleteMode) {
        deleteMode = false
    }

    Column(modifier = Modifier.padding(start = ScreenPaddingLeft, top = 20.dp)) {
        Row {
            Text(
                modifier = Modifier.alignByBaseline(),
                text = "插件列表",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .alignByBaseline()
                    .basicMarquee(),
                text = "内部插件目录: ${PluginManager.getPluginDir().absolutePath}",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                modifier = Modifier.padding(end = 10.dp),
                onClick = { deleteMode = !deleteMode }
            ) {
                Text(if (deleteMode) "退出" else "删除模式")
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Icon(
                    imageVector = if (deleteMode) Icons.Outlined.Check else Icons.Outlined.Delete,
                    contentDescription = if (deleteMode) "退出" else "删除模式"
                )
            }
        }
        LazyVerticalGrid(
            modifier = Modifier
                .padding(start = 0.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
                .weight(1f),
            columns = GridCells.Fixed(3)
        ) {
            items(loadedPlugins.plugins) {
                WideClassicCard(
                    modifier = Modifier
                        .focusOnMount(it.packageName)
                        .padding(10.dp),
                    onClick = {
                        if (deleteMode) {
                            if (it.isExternalPlugin) {
                                toastController.warning("外部插件请删除安装的APP")
                            } else {
                                popUninstallPluginDrawer(
                                    pluginInfo = it,
                                    pluginManageScreenViewModel = pluginManageScreenViewModel,
                                    drawerController = drawerController,
                                    onSuccess = {
                                        pluginManageScreenViewModel.refreshPluginInfoList()
                                    },
                                    onFailure = {
                                        toastController.error("删除失败${it?.message ?: ""}")
                                    }
                                )
                            }
                        } else {
                            pluginManageScreenViewModel.launchPlugin(
                                pluginInfo = it,
                                onSuccess = {
                                    lastFocusedItemPerDestination[SPECIAL_DESTINATION_PLUGIN_HOME] =
                                        "$HOME_FIRST_ROW_FOCUS_ON_MOUNT_KEY, col 0"
                                    navController.nav(NavigationItems.PluginHome)
                                },
                                onFailure = {
                                    toastController.error(it)
                                }
                            )
                        }
                    },
                    image = {
                        AdaptiveIconImage(
                            modifier = Modifier.size(68.dp),
                            drawable = it.icon
                        )
                    },
                    title = {
                        Text(
                            modifier = Modifier.padding(start = 5.dp),
                            text = it.name,
                            maxLines = 1
                        )
                    },
                    subtitle = {
                        Column(modifier = Modifier.padding(start = 5.dp)) {
                            Text(
                                modifier = Modifier.basicMarquee(),
                                text = it.packageName,
                                maxLines = 1
                            )
                            Text(text = "${it.versionName}(${it.versionCode})", maxLines = 1)
                            Row {
                                val style = LocalTextStyle.current
                                val s = remember { style.copy(fontSize = style.fontSize.times(0.8f)) }
                                if (apiVersion != it.apiVersion) {
                                    Text(
                                        text = "API版本不一致(API:${it.apiVersion})",
                                        color = Color(0XFF_FB_65_42),
                                        style = s
                                    )
                                }
                                if (it.isExternalPlugin) {
                                    Text(
                                        modifier = Modifier
                                            .conditional(apiVersion != it.apiVersion) {
                                                padding(start = 5.dp)
                                            },
                                        text = "外部",
                                        color = Color(0XFF_FB_65_42),
                                        style = style.copy(fontSize = style.fontSize.times(0.8f))
                                    )
                                }
                            }
                        }
                    })
            }

            items(loadedPlugins.invalidFiles) {
                WideClassicCard(
                    modifier = Modifier.padding(10.dp),
                    onClick = {
                        if (deleteMode) {
                            popRemoveFileDrawer(
                                file = it,
                                pluginManageScreenViewModel = pluginManageScreenViewModel,
                                drawerController = drawerController,
                                onSuccess = {
                                    pluginManageScreenViewModel.refreshPluginInfoList()
                                },
                                onFailure = {
                                    toastController.error("删除失败${it?.message ?: ""}")
                                }
                            )
                        } else {
                            toastController.warning("无效插件, 可以删除该文件")
                        }
                    },
                    image = {
                        Icon(
                            modifier = Modifier
                                .size(68.dp)
                                .padding(2.dp),
                            imageVector = Icons.Default.Warning,
                            contentDescription = "无效插件",
                        )
                    },
                    title = {
                        Text(
                            modifier = Modifier.padding(start = 5.dp),
                            text = it.name,
                            maxLines = 1
                        )
                    },
                    subtitle = {
                        val style = LocalTextStyle.current
                        val s = remember { style.copy(fontSize = style.fontSize.times(0.8f)) }
                        if (it.isDirectory && it.name == "oat") {
                            Text(
                                modifier = Modifier.padding(start = 5.dp),
                                text = "OAT文件夹",
                                style = s
                            )
                        } else {
                            Text(
                                modifier = Modifier.padding(start = 5.dp),
                                text = "无效${if(it.isDirectory) "文件夹" else ""}",
                                style = s
                            )
                        }
                    }
                )
            }

            item {
                WideClassicCard(
                    modifier = Modifier.padding(10.dp),
                    onClick = {
                        deleteMode = false
                        if (hasPermission) {
                            popPluginInstall(
                                drawerController = drawerController,
                                toastController = toastController,
                                pluginManageScreenViewModel = pluginManageScreenViewModel
                            )
                        } else {
                            permissionRequester.launch(APP_PERMISSIONS.toTypedArray())
                        }
                    },
                    image = {
                        Icon(
                            modifier = Modifier
                                .size(68.dp)
                                .padding(2.dp),
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "添加",
                        )
                    },
                    title = {
                        Text(modifier = Modifier.padding(start = 5.dp), text = "添加插件")
                    },
                )
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterEnd)
                    .graphicsLayer { alpha = 0.6f },
                text = "APP版本: ${AppUtil.getVersionInfo(context)} API:$apiVersion${if (AppUtil.debuggable(context)) " DEBUG" else ""}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}