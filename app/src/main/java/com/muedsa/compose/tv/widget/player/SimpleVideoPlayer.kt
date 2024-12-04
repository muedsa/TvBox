package com.muedsa.compose.tv.widget.player

import android.annotation.SuppressLint
import android.view.Gravity
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
    playerControlState: PlayerControlState = rememberPlayerControlState(),
    playerBuilderSetting: ExoPlayer.Builder.() -> Unit = {},
    playerInit: ExoPlayer.() -> Unit
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .also(playerBuilderSetting)
            .build()
            .also {
                if (playerControlState.debugMode) {
                    it.addAnalyticsListener(EventLogger())
                }
                it.playerInit()
            }
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

    PlayerControl(state = playerControlState, player = exoPlayer)
}