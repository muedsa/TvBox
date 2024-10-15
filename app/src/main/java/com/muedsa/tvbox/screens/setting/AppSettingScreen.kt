package com.muedsa.tvbox.screens.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedIconButton
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.muedsa.compose.tv.theme.surfaceContainer
import com.muedsa.compose.tv.widget.FocusScaleSwitch
import com.muedsa.util.AppUtil

@Composable
fun AppSettingScreen(
    appSettingScreenViewModel: AppSettingScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settingModel by appSettingScreenViewModel.settingSF.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight(),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(all = 20.dp)
            ) {
                Text(
                    text = "设置",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "全局弹幕开关",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    FocusScaleSwitch(
                        checked = settingModel.danmakuEnable,
                        onCheckedChange = {
                            appSettingScreenViewModel.changeDanmakuEnable(it)
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "合并弹幕",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    FocusScaleSwitch(
                        checked = settingModel.danmakuMergeEnable,
                        onCheckedChange = {
                            appSettingScreenViewModel.changeDanmakuMergeEnable(it)
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "弹幕缩放",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedIconButton(onClick = {
                            appSettingScreenViewModel.changeDanmakuSizeScale(settingModel.danmakuSizeScale - 5)
                        }) {
                            Icon(imageVector = Icons.Outlined.Remove, contentDescription = "-")
                        }
                        Text(
                            modifier = Modifier.width(60.dp),
                            text = "${settingModel.danmakuSizeScale}%",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        OutlinedIconButton(onClick = {
                            appSettingScreenViewModel.changeDanmakuSizeScale(settingModel.danmakuSizeScale + 5)
                        }) {
                            Icon(imageVector = Icons.Outlined.Add, contentDescription = "+")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "弹幕透明度",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedIconButton(onClick = {
                            appSettingScreenViewModel.changeDanmakuAlpha(settingModel.danmakuAlpha - 5)
                        }) {
                            Icon(imageVector = Icons.Outlined.Remove, contentDescription = "-")
                        }

                        Text(
                            modifier = Modifier.width(60.dp),
                            text = "${settingModel.danmakuAlpha}%",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )

                        OutlinedIconButton(onClick = {
                            appSettingScreenViewModel.changeDanmakuAlpha(settingModel.danmakuAlpha + 5)
                        }) {
                            Icon(imageVector = Icons.Outlined.Add, contentDescription = "+")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "弹幕屏占比",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedIconButton(onClick = {
                            appSettingScreenViewModel.changeDanmakuScreenPart(settingModel.danmakuScreenPart - 5)
                        }) {
                            Icon(imageVector = Icons.Outlined.Remove, contentDescription = "-")
                        }
                        Text(
                            modifier = Modifier.width(60.dp),
                            text = "${settingModel.danmakuScreenPart}%",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        OutlinedIconButton(onClick = {
                            appSettingScreenViewModel.changeDanmakuScreenPart(settingModel.danmakuScreenPart + 5)
                        }) {
                            Icon(imageVector = Icons.Outlined.Add, contentDescription = "+")
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APP版本",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = AppUtil.getVersionInfo(context),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (AppUtil.debuggable(context)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "调试模式",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "启用",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }


}