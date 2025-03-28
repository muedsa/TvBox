package com.muedsa.tvbox.screens.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ErrorScreen
import com.muedsa.compose.tv.widget.LoadingScreen
import com.muedsa.tvbox.screens.NavigationItems

@Composable
fun PlaybackScreen(
    navItem: NavigationItems.Player,
    playbackScreenViewModel: PlaybackScreenViewModel = hiltViewModel()
) {
    val toastController = useLocalToastMsgBoxController()
    val uiState by playbackScreenViewModel.uiState.collectAsState()

    LaunchedEffect(navItem) {
        playbackScreenViewModel.load(navItem)
    }

    when (val s = uiState) {
        is PlayBackScreenUiState.Loading -> LoadingScreen()

        is PlayBackScreenUiState.Error -> ErrorScreen(
            onError = {
                toastController.error(s.error)
            },
        )

        is PlayBackScreenUiState.Ready -> PlaybackWidget(
            urls = s.urls,
            httpHeaders = s.httpHeaders,
            episodeProgress = s.episodeProgress,
            danmakuList = s.danmakuList,
            danmakuDataFlow = s.danmakuDataFlow,
            appSetting = s.appSetting,
            disableEpisodeProgression = s.disableEpisodeProgression,
            playbackScreenViewModel = playbackScreenViewModel
        )
    }
}