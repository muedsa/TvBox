package com.muedsa.tvbox.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.WideButton
import com.muedsa.compose.tv.focusOnMount
import com.muedsa.compose.tv.theme.CommonRowCardPadding
import com.muedsa.tvbox.api.data.MediaEpisode
import com.muedsa.tvbox.model.DanmakuEpisode
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import kotlin.math.max

const val EpisodePageSize = 20
val EpisodeProgressStrokeWidth = 12.dp
val WideButtonCornerRadius = 12.dp

@Composable
fun EpisodeListWidget(
    modifier: Modifier = Modifier,
    episodeList: List<MediaEpisode>,
    danmakuEpisodeList: List<DanmakuEpisode>,
    episodeProgressMap: Map<String, EpisodeProgressModel>,
    episodeRelationMap: Map<String, String>,
    enabled: Boolean = true,
    onEpisodeClick: (MediaEpisode, DanmakuEpisode?) -> Unit = { _, _ -> },
    onChangeEpisodeRelation: (List<Pair<String, DanmakuEpisode>>) -> Unit = {}
) {

    val episodeListChunks = episodeList.chunked(EpisodePageSize)
    val danmakuEpisodeListChunks = danmakuEpisodeList.chunked(EpisodePageSize)

    var changeDanmakuEpisodeMode by remember { mutableStateOf(false) }
    var selectedEpisodeIndex by remember { mutableIntStateOf(0) }
    var selectedEpisode by remember { mutableStateOf(episodeList[0]) }

    BackHandler(enabled = changeDanmakuEpisodeMode) {
        changeDanmakuEpisodeMode = false
    }

    Crossfade(
        targetState = changeDanmakuEpisodeMode,
        label = "changeDanmakuEpisodeCrossFade",
    ) {
        Column(modifier = modifier) {
            if (!it) {
                episodeListChunks.forEachIndexed { chunkIndex, currentPartEpisodeList ->
                    val episodePartStartNo = 1 + chunkIndex * EpisodePageSize
                    val episodePartEndNo = episodePartStartNo - 1 + currentPartEpisodeList.size
                    // 剧集标题
                    Row(
                        modifier = Modifier.padding(
                            start = CommonRowCardPadding,
                            bottom = CommonRowCardPadding
                        ),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "剧集 ${episodePartStartNo}-${episodePartEndNo}",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        if (chunkIndex == 0 && danmakuEpisodeList.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(CommonRowCardPadding))
                            Text(
                                text = "长按更改匹配的弹幕剧集",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }

                    // 剧集列表
                    LazyRow {
                        itemsIndexed(
                            items = currentPartEpisodeList,
                        ) { episodePartIndex, episode ->
                            val interactionSource = remember { MutableInteractionSource() }
                            val focusedState = interactionSource.collectIsFocusedAsState()
                            WideButton(
                                modifier = Modifier
                                    .focusOnMount(itemKey = "episode row $chunkIndex, col $episodePartIndex")
                                    .padding(end = 12.dp)
                                    .drawWithCache {
                                        // 进度条
                                        val model = episodeProgressMap[episode.id]
                                        val strokeWidthPx = EpisodeProgressStrokeWidth.toPx()
                                        val wideButtonCornerRadiusPx = WideButtonCornerRadius.toPx()
                                        val width = if (model != null) {
                                            val progressRatio =
                                                model.progress.toFloat() / model.duration
                                            size.width * max(progressRatio, 0.1f)
                                        } else 0f
                                        val height = strokeWidthPx / 2
                                        val cornerRadius = CornerRadius(
                                            wideButtonCornerRadiusPx,
                                            wideButtonCornerRadiusPx
                                        )
                                        onDrawWithContent {
                                            drawContent()
                                            if (width > 0) {
                                                scale(if (focusedState.value) 1.1f else 1f) {
                                                    clipRect(
                                                        right = width,
                                                        bottom = height
                                                    ) {
                                                        drawRoundRect(
                                                            color = Color.Red,
                                                            cornerRadius = cornerRadius
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    },
                                enabled = enabled,
                                interactionSource = interactionSource,
                                title = {
                                    Text(text = episode.name, overflow = TextOverflow.Ellipsis)
                                },
                                subtitle = {
                                    val danmakuEpisode = getDanmakuEpisode(
                                        episode = episode,
                                        episodeIndex = episodePartIndex + chunkIndex * EpisodePageSize,
                                        danEpisodeList = danmakuEpisodeList,
                                        episodeRelationMap = episodeRelationMap,
                                    )
                                    if (danmakuEpisode != null) {
                                        Text(
                                            text = danmakuEpisode.episodeName,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                },
                                onClick = {
                                    onEpisodeClick(
                                        episode,
                                        getDanmakuEpisode(
                                            episode = episode,
                                            episodeIndex = episodePartIndex + chunkIndex * EpisodePageSize,
                                            danEpisodeList = danmakuEpisodeList,
                                            episodeRelationMap = episodeRelationMap,
                                        )
                                    )
                                },
                                onLongClick = {
                                    if (danmakuEpisodeList.isNotEmpty()) {
                                        val index = episodePartIndex + chunkIndex * EpisodePageSize
                                        selectedEpisodeIndex = index
                                        selectedEpisode = episodeList[index]
                                        changeDanmakuEpisodeMode = true
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.width(100.dp))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.padding(
                        start = CommonRowCardPadding,
                        bottom = CommonRowCardPadding
                    ),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "更改弹幕剧情匹配模式",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(CommonRowCardPadding))
                    Text(
                        text = "选择一个弹弹Play剧集来匹配:",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    Text(
                        text = selectedEpisode.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }

                danmakuEpisodeListChunks.forEachIndexed { danChunkIndex, danmakuEpisodeList ->
                    val episodePartStartNo = 1 + danChunkIndex * EpisodePageSize
                    val episodePartEndNo = episodePartStartNo - 1 + danmakuEpisodeList.size
                    Row(
                        modifier = Modifier.padding(
                            start = CommonRowCardPadding,
                            bottom = CommonRowCardPadding
                        ),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "剧集 ${episodePartStartNo}-${episodePartEndNo}",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        if (danChunkIndex == 0) {
                            Spacer(modifier = Modifier.width(CommonRowCardPadding))
                            Text(
                                text = "长按使接下来的剧集都按当前选择的剧集依次匹配",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }

                    LazyRow {
                        itemsIndexed(
                            items = danmakuEpisodeList,
                        ) { danEpisodePartIndex, danmakuEpisode ->
                            val interactionSource = remember { MutableInteractionSource() }
                            WideButton(
                                modifier = Modifier.padding(end = 12.dp),
                                title = {
                                    Text(
                                        text = danmakuEpisode.episodeName,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                subtitle = {
                                    val isFocused by interactionSource.collectIsFocusedAsState()
                                    if (isFocused) {
                                        Text(
                                            modifier = Modifier.basicMarquee(),
                                            text = "选此匹配: ${selectedEpisode.name}",
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                onClick = {
                                    onChangeEpisodeRelation(listOf(selectedEpisode.id to danmakuEpisode))
                                    changeDanmakuEpisodeMode = false
                                },
                                onLongClick = {
                                    onChangeEpisodeRelation(buildList {
                                        var danEpisodePos =
                                            danEpisodePartIndex + danChunkIndex * EpisodePageSize
                                        for (episodePos in selectedEpisodeIndex..<episodeList.size) {
                                            if (danEpisodePos >= danmakuEpisodeList.size) {
                                                break
                                            }
                                            add(episodeList[episodePos].id to danmakuEpisodeList[danEpisodePos])
                                            danEpisodePos++
                                        }
                                    })
                                    changeDanmakuEpisodeMode = false
                                },
                                interactionSource = interactionSource
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.width(100.dp))
                        }
                    }
                }
            }
        }
    }
}

fun getDanmakuEpisode(
    episode: MediaEpisode,
    episodeIndex: Int,
    danEpisodeList: List<DanmakuEpisode>,
    episodeRelationMap: Map<String, String>
): DanmakuEpisode? {
    var danEpisode: DanmakuEpisode? = null

    if (danEpisodeList.isNotEmpty()) {
        val episodeId = episodeRelationMap[episode.id]
        if (episodeId != null) {
            val danEpisodeOptional = danEpisodeList.stream()
                .filter { it.episodeId == episodeId }
                .findFirst()
            if (danEpisodeOptional.isPresent) {
                danEpisode = danEpisodeOptional.get()
            }
        } else {
            if (episodeIndex < danEpisodeList.size) {
                danEpisode = danEpisodeList[episodeIndex]
            }
        }
    }
    return danEpisode
}