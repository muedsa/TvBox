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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.widget.RightSideDrawerController
import com.muedsa.tvbox.plugin.PluginInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

fun popUninstallPluginDrawer(
    pluginInfo: PluginInfo,
    drawerController: RightSideDrawerController,
    onSuccess: () -> Unit,
    onFailure: (Throwable?) -> Unit
) {
    popRemoveFileDrawer(
        file = File(pluginInfo.sourcePath),
        title = "确定删除插件?",
        content = "${pluginInfo.name}\n" +
                "${pluginInfo.packageName}\n" +
                pluginInfo.sourcePath,
        drawerController = drawerController,
        onSuccess = onSuccess,
        onFailure = onFailure
    )
}

fun popRemoveFileDrawer(
    file: File,
    title: String = "确定删除文件${if(file.isDirectory) "夹" else ""}?",
    content: String = file.name,
    drawerController: RightSideDrawerController,
    onSuccess: () -> Unit,
    onFailure: (Throwable?) -> Unit
) {

    drawerController.pop {
        val scope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                modifier = Modifier
                    .width(300.dp)
                    .basicMarquee(),
                text = content,
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
                        scope.launch(Dispatchers.IO) {
                            try {
                                if (file.deleteRecursively()) {
                                    withContext(Dispatchers.Main) {
                                        drawerController.close()
                                        onSuccess()
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        drawerController.close()
                                        onFailure(null)
                                    }
                                }
                            } catch (throwable: Throwable) {
                                Timber.e(throwable)
                                withContext(Dispatchers.Main) {
                                    drawerController.close()
                                    onFailure(throwable)
                                }
                            }
                        }
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