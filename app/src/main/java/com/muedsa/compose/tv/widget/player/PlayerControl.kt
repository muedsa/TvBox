package com.muedsa.compose.tv.widget.player

import android.icu.text.SimpleDateFormat
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.focusOnInitial
import com.muedsa.compose.tv.widget.OutlinedIconBox
import com.muedsa.util.AppUtil
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(UnstableApi::class)
@Composable
fun PlayerControl(
    state: PlayerControlState = rememberPlayerControlState(),
    player: Player,
) {
    var leftArrowBtnPressed by remember { mutableStateOf(false) }
    var rightArrowBtnPressed by remember { mutableStateOf(false) }
    var playBtnPressed by remember { mutableStateOf(false) }

    BackHandler(enabled = state.tick > 0L) {
        state.tick = 0L
    }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(state.loopDelay)
            if (state.tick > 0 && !leftArrowBtnPressed && !rightArrowBtnPressed) {
                state.tick--
            }
            if (player.isCurrentMediaItemSeekable) {
                if (leftArrowBtnPressed) {
                    if (state.seekMs > 0L) state.seekMs = 0L
                    state.seekMs -= state.onceSeekMs
                }
                if (rightArrowBtnPressed) {
                    if (state.seekMs < 0L) state.seekMs = 0L
                    state.seekMs += state.onceSeekMs
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .focusOnInitial()
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MENU
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                        || it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                    ) {
                        state.tick = 25
                    }
                }
                if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        leftArrowBtnPressed = true
                    } else if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        leftArrowBtnPressed = false
                        if (state.seekMs < 0) {
                            player.seekTo(max(0, player.currentPosition + state.seekMs))
                        } else {
                            player.seekBack()
                        }
                        state.seekMs = 0
                    }
                } else if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        rightArrowBtnPressed = true
                    } else if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        rightArrowBtnPressed = false
                        if (state.seekMs > 0) {
                            player.seekTo(
                                min(
                                    player.duration,
                                    player.currentPosition + state.seekMs
                                )
                            )
                        } else {
                            player.seekForward()
                        }
                        state.seekMs = 0
                    }
                } else if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        playBtnPressed = true
                    } else if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        playBtnPressed = false
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                        state.seekMs = 0
                    }
                } else if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        playBtnPressed = true
                    } else if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        playBtnPressed = false
                        if (!player.isPlaying) {
                            player.play()
                        }
                    }
                } else if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        playBtnPressed = true
                    } else if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        playBtnPressed = false
                        if (player.isPlaying) {
                            player.pause()
                        }
                    }
                }
                return@onPreviewKeyEvent false
            }
    ) {

        AnimatedVisibility(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)),
            visible = state.tick > 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp)
            ) {
                Text(
                    text = "视频信息",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = state.videoInfo,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                PlayerProgressIndicator(player = player, seekMs = state.seekMs)
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedIconBox(scaleUp = leftArrowBtnPressed) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "后退")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    OutlinedIconBox(
                        scaleUp = playBtnPressed,
                        inverse = true
                    ) {
                        if (player.isPlaying) {
                            Icon(
                                Icons.Outlined.Pause,
                                contentDescription = "暂停"
                            )
                        } else {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = "播放")
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    OutlinedIconBox(scaleUp = rightArrowBtnPressed) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "前进")
                    }
                }

                if (state.debugMode) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "displayTick: ${state.tick}", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun PlayerProgressIndicator(player: Player, seekMs: Long) {
    val dateTimeFormat = remember { SimpleDateFormat.getDateTimeInstance() }
    var systemStr by remember { mutableStateOf("--/--/-- --:--:--") }
    var currentStr by remember { mutableStateOf("--:--:--") }
    var totalStr by remember { mutableStateOf("--:--:--") }
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (player.duration > 0L) {
            LinearProgressIndicator(
                progress = { (player.currentPosition.toFloat() + seekMs) / player.duration },
                modifier = Modifier.fillMaxWidth(),
                color = if (seekMs == 0L) ProgressIndicatorDefaults.linearColor else Color.Green.copy(
                    alpha = 0.7f
                ),
                gapSize = 0.dp
            ) { }
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "$systemStr    $currentStr / $totalStr",
            textAlign = TextAlign.Right,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }

    LaunchedEffect(key1 = player, key2 = seekMs) {
        while (true) {
            systemStr = dateTimeFormat.format(Date())
            currentStr =
                if (player.duration > 0L) {
                    if (seekMs != 0L) {
                        "${durationToString(player.currentPosition)} (${if (seekMs.sign > 0) "+" else "-"}${seekMs.absoluteValue / 1000}s = ${
                            durationToString(
                                player.currentPosition + seekMs
                            )
                        })"
                    } else {
                        durationToString(player.currentPosition)
                    }
                } else "--:--:--"
            totalStr = if (player.duration > 0L) durationToString(player.duration) else "--:--:--"
            delay(300.milliseconds)
        }
    }
}

fun groupTypeToString(group: Tracks.Group): String {
    return when (group.type) {
        C.TRACK_TYPE_NONE -> "NONE"
        C.TRACK_TYPE_UNKNOWN -> "UNKNOWN"
        C.TRACK_TYPE_DEFAULT -> "DEFAULT"
        C.TRACK_TYPE_AUDIO -> "AUDIO"
        C.TRACK_TYPE_VIDEO -> "VIDEO"
        C.TRACK_TYPE_TEXT -> "TEXT"
        C.TRACK_TYPE_IMAGE -> "IMAGE"
        C.TRACK_TYPE_METADATA -> "METADATA"
        C.TRACK_TYPE_CAMERA_MOTION -> "CAMERA_MOTION"
        else -> "OTHER"
    }
}

fun durationToString(duration: Long): String {
    return duration.toDuration(DurationUnit.MILLISECONDS)
        .toComponents { hours, minutes, seconds, _ ->
            String.format(
                locale = Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds,
            )
        }
}

@Stable
class PlayerControlState(
    initLoopDelayMs: Long = 200L,
    initMaxDisplayTicks: Int = 25,
    initOnceSeekMs: Long = 5000L,
    initDebugMode: Boolean = false,
    initVideoInfo: String = "",
) {
    var loopDelay by mutableStateOf(initLoopDelayMs.milliseconds)
    var tick by mutableLongStateOf(0L)
    var maxDisplayTicks by mutableIntStateOf(initMaxDisplayTicks)
    var seekMs by mutableLongStateOf(0L)
    var onceSeekMs by mutableLongStateOf(initOnceSeekMs)
    var debugMode by mutableStateOf(initDebugMode)
    var videoInfo by mutableStateOf(initVideoInfo)

    companion object {
        private const val LOOP_DELAY_KEY = "LOOP_DELAY"
        private const val MAX_DISPLAY_TICKS_KEY = "MAX_DISPLAY_TICKS"
        private const val ONCE_SEEK_MS_KEY = "ONCE_SEEK_MS"
        private const val DEBUG_MODE_KEY = "DEBUG_MODE"
        private const val VIDEO_INFO_KEY = "VIDEO_INFO"

        val Saver: Saver<PlayerControlState, *> = mapSaver(
            save = {
                mutableMapOf<String, Any?>().apply {
                    put(LOOP_DELAY_KEY, it.loopDelay.inWholeMilliseconds)
                    put(MAX_DISPLAY_TICKS_KEY, it.maxDisplayTicks)
                    put(ONCE_SEEK_MS_KEY, it.onceSeekMs)
                    put(DEBUG_MODE_KEY, it.debugMode)
                    put(VIDEO_INFO_KEY, it.videoInfo)
                }
            },
            restore = {
                PlayerControlState(
                    initLoopDelayMs = it[LOOP_DELAY_KEY] as Long? ?: 200L,
                    initMaxDisplayTicks = it[MAX_DISPLAY_TICKS_KEY] as Int? ?: 25,
                    initOnceSeekMs = it[ONCE_SEEK_MS_KEY] as Long? ?: 5000L,
                    initDebugMode = it[DEBUG_MODE_KEY] as Boolean? == true,
                    initVideoInfo = it[VIDEO_INFO_KEY] as String? ?: "",
                )
            }
        )
    }
}

@Composable
fun rememberPlayerControlState(
    initLoopDelayMs: Long = 200L,
    initMaxDisplayTicks: Int = 25,
    initOnceSeekMs: Long = 5000L,
    initDebugMode: Boolean = AppUtil.debuggable(LocalContext.current),
    initVideoInfo: String = "",
): PlayerControlState {
    return rememberSaveable(saver = PlayerControlState.Saver) {
        PlayerControlState(
            initLoopDelayMs = initLoopDelayMs,
            initMaxDisplayTicks = initMaxDisplayTicks,
            initOnceSeekMs = initOnceSeekMs,
            initDebugMode = initDebugMode,
            initVideoInfo = initVideoInfo,
        )
    }
}