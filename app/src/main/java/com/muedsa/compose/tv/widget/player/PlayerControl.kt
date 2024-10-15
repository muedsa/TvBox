package com.muedsa.compose.tv.widget.player

import android.icu.text.SimpleDateFormat
import android.view.KeyEvent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.muedsa.compose.tv.focusOnInitial
import com.muedsa.compose.tv.widget.OutlinedIconBox
import kotlinx.coroutines.delay
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(UnstableApi::class)
@Composable
fun PlayerControl(
    debug: Boolean = false,
    player: Player,
    state: MutableState<Int> = remember { mutableIntStateOf(0) },
) {
    var leftArrowBtnPressed by remember { mutableStateOf(false) }
    var rightArrowBtnPressed by remember { mutableStateOf(false) }
    var playBtnPressed by remember { mutableStateOf(false) }
    var videoInfo by remember { mutableStateOf("") }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(1.seconds)
            if (state.value > 0) {
                state.value--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .focusOnInitial()
            .onPreviewKeyEvent {
                if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
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
                        state.value = 5
                    }
                }
                if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        leftArrowBtnPressed = true
                    } else if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        leftArrowBtnPressed = false
                        player.seekBack()
                    }
                } else if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                        rightArrowBtnPressed = true
                    } else if (it.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                        rightArrowBtnPressed = false
                        player.seekForward()
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
            visible = state.value > 0,
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
                    text = videoInfo,
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
                PlayerProgressIndicator(player)
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

                if (debug) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "show: $state", color = Color.Red)
                }
            }
        }
    }

    LaunchedEffect(key1 = player) {
        player.addListener(object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                var newVideoInfo = ""
                for (trackGroup in tracks.groups) {
                    if (trackGroup.isSelected) {
                        var groupInfo = "group [ type=${groupTypeToString(trackGroup)}\n"
                        for (i in 0 until trackGroup.length) {
                            val isSelected = trackGroup.isTrackSelected(i)
                            if (isSelected) {
                                val trackFormat = trackGroup.getTrackFormat(i)
                                groupInfo += "    Track ${Format.toLogString(trackFormat)}\n"
                            }
                        }
                        groupInfo += "]\n"
                        newVideoInfo += groupInfo
                    }
                }
                videoInfo = newVideoInfo
            }
        })
    }
}

@Composable
fun PlayerProgressIndicator(player: Player) {
    val dateTimeFormat = remember { SimpleDateFormat.getDateTimeInstance() }
    var systemStr by remember { mutableStateOf("--/--/-- --:--:--") }
    var currentStr by remember { mutableStateOf("--:--:--") }
    var totalStr by remember { mutableStateOf("--:--:--") }
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (player.duration > 0L) {
            LinearProgressIndicator(
                progress = { player.currentPosition.toFloat() / player.duration },
                modifier = Modifier.fillMaxWidth(),
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

    LaunchedEffect(key1 = Unit) {
        while (true) {
            systemStr = dateTimeFormat.format(Date())
            currentStr =
                if (player.duration > 0L) durationToString(player.currentPosition) else "--:--:--"
            totalStr = if (player.duration > 0L) durationToString(player.duration) else "--:--:--"
            delay(100.microseconds)
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