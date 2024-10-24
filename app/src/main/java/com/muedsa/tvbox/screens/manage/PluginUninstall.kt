package com.muedsa.tvbox.screens.manage

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.widget.RightSideDrawerController
import com.muedsa.tvbox.plugin.PluginInfo
import java.io.File

fun popUninstallPluginDrawer(
    pluginInfo: PluginInfo,
    pluginManageScreenViewModel: PluginManageScreenViewModel,
    drawerController: RightSideDrawerController,
    onSuccess: () -> Unit,
    onFailure: (Throwable?) -> Unit
) {
    drawerController.pop {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "确定删除插件?",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier
                    .width(300.dp)
                    .basicMarquee(),
                text = pluginInfo.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .width(300.dp)
                    .basicMarquee(),
                text = pluginInfo.packageName,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier
                    .width(300.dp)
                    .basicMarquee(),
                text = pluginInfo.sourcePath,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(100.dp))
            Row(
                modifier = Modifier.width(300.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        pluginManageScreenViewModel.uninstallPlugin(
                            pluginInfo = pluginInfo,
                            onSuccess = {
                                drawerController.close()
                                onSuccess()
                            },
                            onFailure = {
                                drawerController.close()
                                onFailure(it)
                            }
                        )
                    }
                ) {
                    Text("确定")
                }
                Button(
                    onClick = {
                        drawerController.close()
                    }
                ) {
                    Text("取消")
                }
            }
        }
    }
}

fun popRemoveFileDrawer(
    file: File,
    pluginManageScreenViewModel: PluginManageScreenViewModel,
    drawerController: RightSideDrawerController,
    onSuccess: () -> Unit,
    onFailure: (Throwable?) -> Unit
) {
    drawerController.pop {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "确定删除文件${if (file.isDirectory) "夹" else ""}?",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier
                    .width(300.dp)
                    .basicMarquee(),
                text = file.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(100.dp))
            Row(
                modifier = Modifier.width(300.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        pluginManageScreenViewModel.deleteFile(
                            file = file,
                            onSuccess = {
                                drawerController.close()
                                onSuccess()
                            }, onFailure = {
                                drawerController.close()
                                onFailure(it)
                            }
                        )
                    }
                ) {
                    Text("确定")
                }

                Button(
                    onClick = {
                        drawerController.close()
                    }
                ) {
                    Text("取消")
                }
            }
        }
    }
}