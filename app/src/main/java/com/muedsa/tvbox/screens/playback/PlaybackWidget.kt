package com.muedsa.tvbox.screens.playback

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.kuaishou.akdanmaku.ecs.component.filter.DuplicateMergedFilter
import com.kuaishou.akdanmaku.ui.DanmakuPlayer
import com.muedsa.compose.tv.useLocalNavHostController
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.AppBackHandler
import com.muedsa.compose.tv.widget.player.DanmakuVideoPlayer
import com.muedsa.compose.tv.widget.player.mergeDanmaku
import com.muedsa.compose.tv.widget.player.rememberPlayerControlState
import com.muedsa.tvbox.api.data.DanmakuDataFlow
import com.muedsa.tvbox.model.AppSettingModel
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.max
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(UnstableApi::class)
@Composable
fun PlaybackWidget(
    urls: List<String>,
    httpHeaders: Map<String, String>?,
    episodeProgress: EpisodeProgressModel,
    danmakuList: List<DanmakuItemData>,
    danmakuDataFlow: DanmakuDataFlow?,
    appSetting: AppSettingModel,
    disableEpisodeProgression: Boolean,
    playbackScreenViewModel: PlaybackScreenViewModel
) {
    val navController = useLocalNavHostController()
    val toastController = useLocalToastMsgBoxController()
    var exoplayerHolder by remember { mutableStateOf<ExoPlayer?>(null) }
    var danmakuPlayerHolder by remember { mutableStateOf<DanmakuPlayer?>(null) }
    var playerEnd by remember { mutableStateOf(false) }

    AppBackHandler {
        toastController.warning("再次点击返回键退出")
    }

    LaunchedEffect(key1 = playerEnd) {
        if (playerEnd) {
            if (exoplayerHolder != null && !disableEpisodeProgression) {
                val exoPlayer = exoplayerHolder!!
                episodeProgress.progress = exoPlayer.duration
                episodeProgress.duration = exoPlayer.duration
                episodeProgress.updateAt = System.currentTimeMillis()
                playbackScreenViewModel.saveEpisodeProgress(episodeProgress)
            }
            toastController.info("播放结束,即将返回")
            delay(3_000)
            navController.popBackStack()
        }
    }

    LaunchedEffect(key1 = exoplayerHolder) {
        if (exoplayerHolder != null && !disableEpisodeProgression) {
            val exoPlayer = exoplayerHolder!!
            while (exoplayerHolder != null) {
                delay(10_000)
                episodeProgress.progress = exoPlayer.currentPosition
                episodeProgress.duration = exoPlayer.duration
                if (episodeProgress.progress + 5_000 > episodeProgress.duration) {
                    episodeProgress.progress = max(episodeProgress.duration - 5_000, 0)
                }
                episodeProgress.updateAt = System.currentTimeMillis()
                playbackScreenViewModel.saveEpisodeProgress(episodeProgress)
            }
        }
    }
    val androidContext = LocalContext.current
    val mediaSourceFactory = remember {
        DefaultMediaSourceFactory(
            DefaultDataSource.Factory(androidContext,
                DefaultHttpDataSource.Factory().apply {
                    if (!httpHeaders.isNullOrEmpty()) {
                        setDefaultRequestProperties(httpHeaders)
                    }
                }
            )
        )
    }
    val playerControlState = rememberPlayerControlState()
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        colors = SurfaceDefaults.colors(
            containerColor = Color.Black,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        DanmakuVideoPlayer(
            playerControlState = playerControlState,
            danmakuConfigSetting = {
                textSizeScale = appSetting.danmakuSizeScale / 100f
                alpha = appSetting.danmakuAlpha / 100f
                screenPart = appSetting.danmakuScreenPart / 100f
                dataFilter = listOf(DuplicateMergedFilter().apply { enable = true })
            },
            danmakuPlayerInit = {
                if (danmakuList.isNotEmpty()) {
                    var list = danmakuList
                    if (appSetting.danmakuMergeEnable) {
                        list = list.mergeDanmaku(5000L, 60000L, 30)
                    }
                    updateData(list)
                }
                danmakuPlayerHolder = this
            },
            enableFSR = appSetting.fsrEnable,
            videoPlayerBuilderSetting = {
                setMediaSourceFactory(mediaSourceFactory)
            }
        ) {
            addListener(object : Player.Listener {

                override fun onPlayerErrorChanged(error: PlaybackException?) {
                    toastController.error(error, SnackbarDuration.Long)
                    error?.let {
                        Timber.i(it, "exoplayer mediaUrl: $urls")
                    }
                }

                override fun onRenderedFirstFrame() {
                    if (exoplayerHolder == null) {
                        exoplayerHolder = this@DanmakuVideoPlayer
                        if (episodeProgress.progress > 0) {
                            val position =
                                if (episodeProgress.progress == episodeProgress.duration) {
                                    // 如果上次已经播放完成则不跳转 从头播放
                                    0
                                } else if (episodeProgress.duration > 5_000 && episodeProgress.progress > episodeProgress.duration - 5_000) {
                                    // 如果太过接近结束的位置
                                    episodeProgress.duration - 5_000
                                } else {
                                    episodeProgress.progress
                                }
                            if (position > 0) {
                                seekTo(position)
                                val positionStr = position
                                    .toDuration(DurationUnit.MILLISECONDS)
                                    .toComponents { hours, minutes, seconds, _ ->
                                        String.format(
                                            locale = java.util.Locale.getDefault(),
                                            "%02d:%02d:%02d",
                                            hours,
                                            minutes,
                                            seconds,
                                        )
                                    }
                                toastController.info("跳转到上次播放位置: $positionStr")
                            }
                        }
                    }
                    if (duration > 600000L) {
                        playerControlState.onceSeekMs = 15000L
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    val ended = playbackState == Player.STATE_ENDED
                    if (playerEnd != ended) {
                        playerEnd = ended
                    }
                }
            })
            playWhenReady = true
            val mediaSources = urls.map { url ->
                val mediaItemBuilder = MediaItem.Builder().setUri(url)
                if (url.endsWith(".m3u8", ignoreCase = true)) {
                    mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
                }
                mediaSourceFactory.createMediaSource(mediaItemBuilder.build())
            }.toTypedArray()
            setMediaSource(MergingMediaSource(*mediaSources))
            prepare()
        }
    }

    LaunchedEffect(danmakuDataFlow) {
        danmakuDataFlow?.flow?.collect { data ->
            danmakuPlayerHolder?.let { danmakuPlayer ->
                if (!danmakuPlayer.isReleased) {
                    danmakuPlayer.send(
                        DanmakuItemData(
                            danmakuId = data.danmakuId,
                            position = if (data.position < 0)
                                danmakuPlayer.getCurrentTimeMs() + 500 else data.position,
                            content = data.content,
                            mode = data.mode,
                            textSize = 25,
                            textColor = data.textColor,
                            score = data.score,
                            danmakuStyle = data.danmakuStyle
                        )
                    )
                }
            }
        }
    }

    DisposableEffect(danmakuDataFlow) {
        onDispose {
            danmakuDataFlow?.close()
        }
    }
}