package com.muedsa.tvbox.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedIconButton
import androidx.tv.material3.RadioButton
import androidx.tv.material3.Text
import androidx.tv.material3.WideButton
import androidx.tv.material3.WideButtonDefaults
import com.muedsa.compose.tv.theme.outline
import com.muedsa.compose.tv.useLocalRightSideDrawerController
import com.muedsa.compose.tv.widget.NoBackground
import com.muedsa.compose.tv.widget.TwoSideWideButton

@Composable
fun DanmakuMediaSelectorWidget(
    enabledDanmakuState: MutableState<Boolean> = remember { mutableStateOf(false) },
    mediaDetailScreenViewModel: MediaDetailScreenViewModel,
) {
    val drawerController = useLocalRightSideDrawerController()

    OutlinedIconButton(onClick = {
        drawerController.pop {
            DanmakuMediaSelectorSideWidget(
                enabledDanmakuState = enabledDanmakuState,
                mediaDetailScreenViewModel = mediaDetailScreenViewModel
            )
        }
    }) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "修改弹幕匹配剧集"
        )
    }
}

@Composable
fun DanmakuMediaSelectorSideWidget(
    enabledDanmakuState: MutableState<Boolean> = remember { mutableStateOf(false) },
    mediaDetailScreenViewModel: MediaDetailScreenViewModel
) {
    val drawerController = useLocalRightSideDrawerController()

    var isSelectorTab by remember { mutableStateOf(true) }

    val ui by mediaDetailScreenViewModel.uiState.collectAsStateWithLifecycle()
    if (ui !is MediaDetailScreenUiState.Ready) {
        return
    }
    val mediaDetail = (ui as MediaDetailScreenUiState.Ready).mediaDetail
    val danmakuMediaList = (ui as MediaDetailScreenUiState.Ready).danmakuMediaList ?: emptyList()
    val danmakuMediaInfo = (ui as MediaDetailScreenUiState.Ready).danmakuMediaInfo

    var searchTitle by remember { mutableStateOf(danmakuMediaInfo?.mediaName ?: mediaDetail.title) }

    val splitTitles = mediaDetail.title.split("\\s+".toRegex())

    Column {
        if (isSelectorTab) {
            Text(
                modifier = Modifier
                    .padding(start = 8.dp, end = 15.dp),
                text = "弹幕剧集",
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                item {
                    val interactionSource = remember { MutableInteractionSource() }
                    TwoSideWideButton(
                        title = { Text("关闭弹幕") },
                        onClick = {
                            drawerController.close()
                            enabledDanmakuState.value = false
                        },
                        interactionSource = interactionSource,
                        background = {
                            WideButtonDefaults.NoBackground(
                                interactionSource = interactionSource
                            )
                        }
                    ) {
                        RadioButton(
                            selected = !enabledDanmakuState.value,
                            onClick = { },
                            interactionSource = interactionSource
                        )
                    }
                }
                items(items = danmakuMediaList, key = { it.mediaName }) {
                    val interactionSource = remember { MutableInteractionSource() }
                    TwoSideWideButton(
                        title = {
                            Text(
                                modifier = Modifier.basicMarquee(),
                                text = "${it.mediaName} - ${it.publishDate}"
                            )
                        },
                        onClick = {
                            drawerController.close()
                            enabledDanmakuState.value = true
                            mediaDetailScreenViewModel.changeDanmakuMedia(it)
                        },
                        interactionSource = interactionSource,
                        background = {
                            WideButtonDefaults.NoBackground(
                                interactionSource = interactionSource
                            )
                        }
                    ) {
                        RadioButton(
                            selected = enabledDanmakuState.value
                                    && danmakuMediaInfo?.mediaId == it.mediaId,
                            onClick = { },
                            interactionSource = interactionSource
                        )
                    }
                }

                item {
                    val interactionSource = remember { MutableInteractionSource() }
                    TwoSideWideButton(
                        title = { Text(text = "未找到？") },
                        onClick = {
                            isSelectorTab = false
                        },
                        interactionSource = interactionSource,
                        background = {
                            WideButtonDefaults.NoBackground(
                                interactionSource = interactionSource
                            )
                        }
                    ) {
                        Text(text = "手动搜索")
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier
                    .padding(start = 8.dp, end = 15.dp),
                text = "手动搜索",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                modifier = Modifier
                    .width(256.dp)
                    .background(
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = OutlinedTextFieldDefaults.shape
                    ),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                value = searchTitle,
                onValueChange = {
                    searchTitle = it
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.width(256.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        mediaDetailScreenViewModel.searchDanmakuMedia(searchTitle)
                        isSelectorTab = true
                    }
                ) {
                    Text("搜索")
                }

                Button(
                    onClick = {
                        isSelectorTab = true
                    }
                ) {
                    Text("返回")
                }
            }

            if (splitTitles.size > 1) {
                splitTitles.forEach {
                    WideButton(
                        modifier = Modifier.padding(top = 10.dp),
                        onClick = {
                            searchTitle = it
                            mediaDetailScreenViewModel.searchDanmakuMedia(searchTitle)
                            isSelectorTab = true
                        },
                        title = {
                            Text(text = it)
                        }
                    )
                }

            }

        }
    }
}