package com.muedsa.tvbox.screens.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.OutlinedIconButton
import androidx.tv.material3.RadioButton
import androidx.tv.material3.Text
import androidx.tv.material3.WideButtonDefaults
import com.muedsa.compose.tv.focusOnInitial
import com.muedsa.compose.tv.model.ContentModel
import com.muedsa.compose.tv.theme.ScreenPaddingLeft
import com.muedsa.compose.tv.useLocalNavHostController
import com.muedsa.compose.tv.useLocalRightSideDrawerController
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ContentBlock
import com.muedsa.compose.tv.widget.ContentBlockType
import com.muedsa.compose.tv.widget.NoBackground
import com.muedsa.compose.tv.widget.ScreenBackground
import com.muedsa.compose.tv.widget.ScreenBackgroundType
import com.muedsa.compose.tv.widget.TwoSideWideButton
import com.muedsa.compose.tv.widget.rememberScreenBackgroundState
import com.muedsa.tvbox.api.data.MediaDetail
import com.muedsa.tvbox.api.data.MediaMergingHttpSource
import com.muedsa.tvbox.model.dandanplay.BangumiInfo
import com.muedsa.tvbox.model.dandanplay.BangumiSearch
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.screens.nav
import com.muedsa.tvbox.screens.plugin.home.MediaCardRow
import com.muedsa.tvbox.theme.FavoriteIconColor
import com.muedsa.tvbox.tool.LenientJson
import kotlinx.serialization.encodeToString
import timber.log.Timber

@Composable
fun MediaDetailWidget(
    pluginInfo: PluginInfo,
    mediaDetail: MediaDetail,
    favorite: Boolean,
    progressMap: Map<String, EpisodeProgressModel>,
    danBangumiList: List<BangumiSearch>?,
    danBangumiInfo: BangumiInfo?,
    mediaDetailScreenViewModel: MediaDetailScreenViewModel
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val toastController = useLocalToastMsgBoxController()
    val drawerController = useLocalRightSideDrawerController()
    val navController = useLocalNavHostController()

    val backgroundState = rememberScreenBackgroundState(
        initType = ScreenBackgroundType.SCRIM
    )

    LaunchedEffect(key1 = mediaDetail) {
        backgroundState.url = mediaDetail.backgroundImageUrl
    }

    ScreenBackground(backgroundState)

    var selectedPlaySourceId by rememberSaveable { mutableStateOf("") }
    val selectedPlaySource = mediaDetail.playSourceList
        .find { it.id == selectedPlaySourceId }
        ?: mediaDetail.playSourceList.firstOrNull()
    val enabledDanmakuState = rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .padding(start = ScreenPaddingLeft),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // top space
        item {
            // 占位锚点 使之可以通过Dpad返回页面的顶部
            Spacer(modifier = Modifier.focusable().focusOnInitial())
        }

        // 介绍
        item {
            ContentBlock(
                modifier = Modifier
                    .padding(top = screenHeight * 0.2f)
                    .width(screenWidth * 0.60f),
                model = ContentModel(
                    title = mediaDetail.title,
                    subtitle = mediaDetail.subTitle,
                    description = mediaDetail.description
                ),
                type = ContentBlockType.CAROUSEL,
                verticalArrangement = Arrangement.Top,
                descriptionMaxLines = 10
            )
            Spacer(modifier = Modifier.height(25.dp))
        }

        // 按钮列表
        item {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(25.dp),
                contentPadding = PaddingValues(end = 25.dp)
            ) {
                // 切换播放源
                if (mediaDetail.playSourceList.isNotEmpty()) {
                    item {
                        Text(
                            text = "播放源: ${selectedPlaySource?.name ?: ""}",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedIconButton(onClick = {
                            drawerController.pop {
                                Column {
                                    Text(
                                        modifier = Modifier
                                            .padding(start = 8.dp, end = 15.dp),
                                        text = "选择播放源",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    LazyColumn(
                                        contentPadding = PaddingValues(vertical = 20.dp)
                                    ) {
                                        items(items = mediaDetail.playSourceList) {
                                            val interactionSource =
                                                remember { MutableInteractionSource() }
                                            TwoSideWideButton(
                                                title = { Text(text = it.name) },
                                                onClick = {
                                                    drawerController.close()
                                                    selectedPlaySourceId = it.id
                                                },
                                                interactionSource = interactionSource,
                                                background = {
                                                    WideButtonDefaults.NoBackground(
                                                        interactionSource = interactionSource
                                                    )
                                                }
                                            ) {
                                                RadioButton(
                                                    selected = selectedPlaySource == it,
                                                    onClick = { },
                                                    interactionSource = interactionSource
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "修改播放源"
                            )
                        }
                    }
                }

                item {
                    // 收藏按钮
                    OutlinedButton(onClick = {
                        mediaDetailScreenViewModel.favorite(
                            pluginInfo = pluginInfo,
                            favoriteMediaCard = mediaDetail.favoritedMediaCard,
                            favorite = !favorite
                        )
                    }) {
                        Text(text = if (favorite) "已收藏" else "收藏")
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "收藏",
                            tint = if (favorite) FavoriteIconColor else LocalContentColor.current
                        )
                    }
                }

                // 切换弹弹Play匹配剧集
                if (danBangumiList != null) {
                    item {
                        Text(
                            text = "弹弹Play匹配剧集: ",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            modifier = Modifier
                                .widthIn(max = 256.dp)
                                .basicMarquee(),
                            text = if (enabledDanmakuState.value)
                                danBangumiInfo?.let { "${it.animeTitle}[Rating ${it.rating}]" } ?: "--"
                            else "关闭",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                        AnimeDanmakuSelectBtnWidget(
                            enabledDanmakuState = enabledDanmakuState,
                            mediaDetailScreenViewModel = mediaDetailScreenViewModel
                        )
                    }
                }

                // 清除视频进度
                if (progressMap.isNotEmpty()) {
                    item {
                        OutlinedButton(
                            onClick = {
                                mediaDetailScreenViewModel.clearProgress(
                                    pluginInfo = pluginInfo,
                                    mediaDetail = mediaDetail
                                )
                            }
                        ) {
                            Text(text = "清除播放进度")
                        }
                    }
                }

                // 设置按钮
                item {
                    OutlinedButton(
                        onClick = {
                            navController.nav(NavigationItems.Setting)
                        }
                    ) {
                        Text(text = "设置")
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 剧集列表
        val episodePlaySource = selectedPlaySource
        val episodeList = episodePlaySource?.episodeList ?: emptyList()
        if (episodePlaySource != null && episodeList.isNotEmpty()) {
            item {
                val episodeRelationMap = remember { mutableStateMapOf<String, Long>() }
                var episodeClickLoading by remember { mutableStateOf(false) }

                EpisodeListWidget(
                    episodeList = episodeList,
                    danEpisodeList = danBangumiInfo?.episodes ?: emptyList(),
                    episodeProgressMap = progressMap,
                    episodeRelationMap = episodeRelationMap,
                    enabled = !episodeClickLoading,
                    onEpisodeClick = { episode, danEpisode ->
                        episodeClickLoading = true
                        Timber.d("click episode ${mediaDetail.id}-${episode.name}")

                        mediaDetailScreenViewModel.getEpisodePlayInfo(
                            playSource = episodePlaySource,
                            episode = episode,
                            onSuccess = {
                                episodeClickLoading = false
                                Timber.d("episode:${mediaDetail.id}-${episode.name}, url:${it.url}")
                                navController.nav(
                                    NavigationItems.Player(
                                        urls = if (it is MediaMergingHttpSource ) it.urls else listOf(it.url),
                                        httpHeadersJson = it.httpHeaders?.let { h -> LenientJson.encodeToString(h) },
                                        pluginPackage = pluginInfo.packageName,
                                        mediaId = mediaDetail.id,
                                        episodeId = episode.id,
                                        danEpisodeId = if (enabledDanmakuState.value && danEpisode != null)
                                            danEpisode.episodeId else -1
                                    )
                                )
                            },
                            onFailure = {
                                episodeClickLoading = false
                                toastController.error(it)
                            },
                        )
                    },
                    onChangeEpisodeRelation = {
                        it.forEach { pair ->
                            episodeRelationMap[pair.first] = pair.second.episodeId
                        }
                    }
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // 关联视频
        items(items = mediaDetail.rows) {
            MediaCardRow(
                row = it,
                onItemClick = { _, mediaCard ->
                    navController.nav(
                        NavigationItems.Detail(
                            pluginPackage = pluginInfo.packageName,
                            id = mediaCard.id,
                            url = mediaCard.detailUrl
                        )
                    )
                }
            )
        }
    }
}
