package com.muedsa.compose.tv.widget.player

import android.annotation.SuppressLint
import android.view.Gravity
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@SuppressLint("OpaqueUnitKey")
@OptIn(UnstableApi::class)
@Composable
fun SimpleVideoPlayer(
    debug: Boolean = false,
    playerBuilderSetting: ExoPlayer.Builder.() -> Unit = {},
    playerInit: ExoPlayer.() -> Unit
) {
    val context = LocalContext.current

    val playerControlTicker = remember { mutableIntStateOf(0) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .also(playerBuilderSetting)
            .build()
            .also {
                if (debug) {
                    it.addAnalyticsListener(EventLogger())
                }
                it.playerInit()
            }
    }

    BackHandler(enabled = playerControlTicker.intValue > 0) {
        playerControlTicker.intValue = 0
    }

    DisposableEffect(
        AndroidView(factory = {
            PlayerView(context).apply {
                hideController()
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                player = exoPlayer
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            }
        })
    ) {
        onDispose {
            exoPlayer.release()
        }
    }

    PlayerControl(debug = debug, player = exoPlayer, state = playerControlTicker)
}