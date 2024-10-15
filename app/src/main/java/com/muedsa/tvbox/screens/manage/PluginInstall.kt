package com.muedsa.tvbox.screens.manage

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.widget.RightSideDrawerController
import com.muedsa.compose.tv.widget.ToastMessageBoxController

fun popPluginInstall(
    drawerController: RightSideDrawerController,
    toastController: ToastMessageBoxController,
    pluginManageScreenViewModel: PluginManageScreenViewModel
) {
    drawerController.pop {
        val pluginFiles by pluginManageScreenViewModel.downloadPluginFilesSF.collectAsState()
        Column(modifier = Modifier.widthIn(max = 300.dp)) {
            Text(text = "选择要安装的文件", style = MaterialTheme.typography.titleLarge)

            if (pluginFiles.isNotEmpty()) {
                LazyColumn {
                    items(pluginFiles) {
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                pluginManageScreenViewModel.installPlugin(
                                    file = it,
                                    onSuccess = {
                                        drawerController.close()
                                        pluginManageScreenViewModel.refreshPluginInfoList()
                                    },
                                    onFailure = {
                                        toastController.error("安装失败${it.message}")
                                    }
                                )
                            },
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .basicMarquee(),
                                text = it.name,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            } else {
                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = "没有在下载目录扫描到插件文件"
                )
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = pluginManageScreenViewModel.downloadDir.absolutePath
                )
                Text(modifier = Modifier.padding(top = 10.dp), text = "或者未授予本APP储存权限")
            }
        }
        LaunchedEffect(Unit) {
            pluginManageScreenViewModel.loadDownloadPluginFiles()
        }
    }
}