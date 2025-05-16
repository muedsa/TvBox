package com.muedsa.compose.tv.widget.player

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import com.kuaishou.akdanmaku.DanmakuConfig
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.kuaishou.akdanmaku.render.SimpleRenderer
import com.kuaishou.akdanmaku.ui.DanmakuPlayer
import com.kuaishou.akdanmaku.ui.DanmakuView
import com.muedsa.compose.tv.widget.player.gl.FsrVideoProcessor
import com.muedsa.compose.tv.widget.player.gl.VideoProcessingGLSurfaceView
import kotlin.random.Random

@SuppressLint("OpaqueUnitKey")
@OptIn(UnstableApi::class)
@Composable
fun DanmakuVideoPlayer(
    playerControlState: PlayerControlState = rememberPlayerControlState(),
    danmakuConfigSetting: DanmakuConfig.() -> Unit = {},
    danmakuPlayerInit: DanmakuPlayer.() -> Unit = {},
    enableFSR: Boolean = false,
    videoPlayerBuilderSetting: ExoPlayer.Builder.() -> Unit = {},
    videoPlayerInit: ExoPlayer.() -> Unit,
) {

    val context = LocalContext.current

    val danmakuConfig = remember {
        DanmakuConfig().apply(danmakuConfigSetting)
    }

    val danmakuPlayer = remember {
        DanmakuPlayer(SimpleRenderer())
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .also(videoPlayerBuilderSetting)
            .build()
            .also {
                if (playerControlState.debugMode) {
                    it.addAnalyticsListener(EventLogger())
                }
                it.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            danmakuPlayer.seekTo(it.currentPosition)
                            danmakuPlayer.start(danmakuConfig)
                        } else {
                            danmakuPlayer.pause()
                        }
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        playerControlState.videoInfo = buildTracksInfo(tracks)
                    }
                })
                it.videoPlayerInit()
            }
    }

    val videoProcessingGLSurfaceView = remember {
        if (enableFSR) {
            VideoProcessingGLSurfaceView(
                context = context,
                requireSecureContext = false,
                videoProcessor = FsrVideoProcessor(context = context),
            )
        } else null
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PlayerView(context).apply {
                hideController()
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                player = exoPlayer
            }.also {
                if (videoProcessingGLSurfaceView != null) {
                    videoProcessingGLSurfaceView.setPlayer(exoPlayer)
                    val contentFrame: FrameLayout = it.findViewById(R.id.exo_content_frame)
                    contentFrame.addView(videoProcessingGLSurfaceView)
                }
            }
        },
        onRelease = {
            exoPlayer.release()
            videoProcessingGLSurfaceView?.setPlayer(null)
        }
    )
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            DanmakuView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }.also {
                danmakuPlayer.bindView(it)
                danmakuPlayer.danmakuPlayerInit()
            }
        },
        onRelease = {
            danmakuPlayer.release()
        }
    )

    PlayerControl( state = playerControlState, player = exoPlayer)
}

fun List<DanmakuItemData>.mergeDanmaku(
    nearMs: Long,
    maxMs: Long,
    mergedSize: Int,
): List<DanmakuItemData> {
    val rollingCache: MutableMap<String, SimilarDanmakuItemData> = mutableMapOf()
    val fixedCache: MutableMap<String, SimilarDanmakuItemData> = mutableMapOf()
    val result = mutableListOf<DanmakuItemData>()
    this.sorted().forEach {
        when (it.mode) {
            DanmakuItemData.DANMAKU_MODE_ROLLING ->
                mergedDanmaku(rollingCache, it, result, nearMs, maxMs, mergedSize)

            DanmakuItemData.DANMAKU_MODE_CENTER_TOP, DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM ->
                mergedDanmaku(rollingCache, it, result, nearMs, maxMs, mergedSize)

            else -> result.add(it)
        }
    }
    rollingCache.forEach { (_, v) -> v.complete(result, mergedSize) }
    fixedCache.forEach { (_, v) -> v.complete(result, mergedSize) }
    return result
}

private fun mergedDanmaku(
    cache: MutableMap<String, SimilarDanmakuItemData>,
    item: DanmakuItemData,
    result: MutableList<DanmakuItemData>,
    nearMs: Long,
    maxMs: Long,
    mergedSize: Int,
) {
    if (item.mergedType != DanmakuItemData.MERGED_TYPE_NORMAL) {
        result.add(item)
        return
    }
    val processedContent = processContent(item.content)
    val existed = cache[processedContent]
    if (existed == null) {
        cache[processedContent] = SimilarDanmakuItemData(processedContent, item)
    } else if (existed.near(nearMs, maxMs, item.position)) {
        existed.inc(item, result)
    } else {
        existed.complete(result, mergedSize)
        cache[processedContent] = SimilarDanmakuItemData(processedContent, item)
        result.add(item.copyWith(mergedType = DanmakuItemData.MERGED_TYPE_ORIGINAL))
    }
}

class SimilarDanmakuItemData(
    val content: String,
    private val first: DanmakuItemData,
) {
    private var last: DanmakuItemData = first
    private var number: Int = 1

    fun near(nearMs: Long, maxMs: Long, pos: Long): Boolean =
        pos - last.position <= nearMs && pos - first.position <= maxMs

    fun inc(item: DanmakuItemData, result: MutableList<DanmakuItemData>) {
        last = item
        number += 1
        result.add(item.copyWith(mergedType = DanmakuItemData.MERGED_TYPE_ORIGINAL))
    }

    private fun createMerged(mergedSize: Int): DanmakuItemData =
        DanmakuItemData(
            danmakuId = Random.nextLong(),
            position = first.position + 100,
            content = "($number)${first.content}",
            mode = first.mode,
            textSize = mergedSize,
            textColor = first.textColor,
            score = first.score,
            danmakuStyle = DanmakuItemData.DANMAKU_STYLE_NONE,
            mergedType = DanmakuItemData.MERGED_TYPE_MERGED
        )

    fun complete(result: MutableList<DanmakuItemData>, mergedSize: Int) {
        if (number == 1) {
            result.add(first)
        } else {
            result.add(first.copyWith(mergedType = DanmakuItemData.MERGED_TYPE_ORIGINAL))
            result.add(createMerged(mergedSize))
        }
    }
}

fun DanmakuItemData.copyWith(
    danmakuId: Long? = null,
    position: Long? = null,
    content: String? = null,
    mode: Int? = null,
    textSize: Int? = null,
    textColor: Int? = null,
    score: Int? = null,
    danmakuStyle: Int? = null,
    userId: Long? = null,
    mergedType: Int? = null,
): DanmakuItemData =
    DanmakuItemData(
        danmakuId = danmakuId ?: this.danmakuId,
        position = position ?: this.position,
        content = content ?: this.content,
        mode = mode ?: this.mode,
        textSize = textSize ?: this.textSize,
        textColor = textColor ?: this.textColor,
        score = score ?: this.score,
        danmakuStyle = danmakuStyle ?: this.danmakuStyle,
        userId = userId ?: this.userId,
        mergedType = mergedType ?: this.mergedType
    )

private fun processContent(content: String): String {
    if (content.isBlank()) {
        return ""
    }
    return removeEndingChars(normalizeContent(content))
}

private val ENDING_CHARS = ".。,，/?？!！…~～@^、+=-_♂♀".toCharArray()
private fun removeEndingChars(content: String): String {
    if (content.isEmpty()) {
        return content
    }
    if (ENDING_CHARS.contains(content.last())) {
        return removeEndingChars(content.substring(0, content.length - 1).trimEnd())
    }
    return content
}

private val REPLACE_CHAR_MAP = mapOf(
    '　' to ' ',
    '１' to '1',
    '２' to '2',
    '３' to '3',
    '４' to '4',
    '５' to '5',
    '６' to '6',
    '７' to '7',
    '８' to '8',
    '９' to '9',
    '０' to '0',
    '!' to '！',
    '＠' to '@',
    '＃' to '#',
    '＄' to '$',
    '％' to '%',
    '＾' to '^',
    '＆' to '&',
    '＊' to '*',
    '（' to '(',
    '）' to ')',
    '－' to '-',
    '＝' to '=',
    '＿' to '_',
    '＋' to '+',
    '［' to '[',
    '］' to ']',
    '｛' to '{',
    '｝' to '}',
    ';' to '；',
    '＇' to '\'',
    ':' to '：',
    '＂' to '\'',
    ',' to '，',
    '．' to '.',
    '／' to '/',
    '＜' to '<',
    '＞' to '>',
    '?' to '？',
    '＼' to '\\',
    '｜' to '|',
    '｀' to '`',
    '～' to '~',
    'ｑ' to 'q',
    'ｗ' to 'w',
    'ｅ' to 'e',
    'ｒ' to 'r',
    'ｔ' to 't',
    'ｙ' to 'y',
    'ｕ' to 'u',
    'ｉ' to 'i',
    'ｏ' to 'o',
    'ｐ' to 'p',
    'ａ' to 'a',
    'ｓ' to 's',
    'ｄ' to 'd',
    'ｆ' to 'f',
    'ｇ' to 'g',
    'ｈ' to 'h',
    'ｊ' to 'j',
    'ｋ' to 'k',
    'ｌ' to 'l',
    'ｚ' to 'z',
    'ｘ' to 'x',
    'ｃ' to 'c',
    'ｖ' to 'v',
    'ｂ' to 'b',
    'ｎ' to 'n',
    'ｍ' to 'm',
    'Ｑ' to 'Q',
    'Ｗ' to 'W',
    'Ｅ' to 'E',
    'Ｒ' to 'R',
    'Ｔ' to 'T',
    'Ｙ' to 'Y',
    'Ｕ' to 'U',
    'Ｉ' to 'I',
    'Ｏ' to 'O',
    'Ｐ' to 'P',
    'Ａ' to 'A',
    'Ｓ' to 'S',
    'Ｄ' to 'D',
    'Ｆ' to 'F',
    'Ｇ' to 'G',
    'Ｈ' to 'H',
    'Ｊ' to 'J',
    'Ｋ' to 'K',
    'Ｌ' to 'L',
    'Ｚ' to 'Z',
    'Ｘ' to 'X',
    'Ｃ' to 'C',
    'Ｖ' to 'V',
    'Ｂ' to 'B',
    'Ｎ' to 'N',
    'Ｍ' to 'M'
)

private fun normalizeContent(content: String): String {
    var c = content
    REPLACE_CHAR_MAP.forEach { (k, v) -> c = c.replace(k, v) }
    return c
}