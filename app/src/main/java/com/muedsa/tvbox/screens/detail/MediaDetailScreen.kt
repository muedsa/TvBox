package com.muedsa.tvbox.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ErrorScreen
import com.muedsa.compose.tv.widget.LoadingScreen

@Composable
fun MediaDetailScreen(
    mediaDetailScreenViewModel: MediaDetailScreenViewModel = hiltViewModel()
) {
    val toastController = useLocalToastMsgBoxController()
    val uiState by mediaDetailScreenViewModel.uiState.collectAsState()

    when (val s = uiState) {
        is MediaDetailScreenUiState.Loading -> LoadingScreen()

        is MediaDetailScreenUiState.Error -> ErrorScreen(onError = { toastController.error(s.error) })

        is MediaDetailScreenUiState.Ready -> MediaDetailWidget(
            pluginInfo = s.pluginInfo,
            mediaDetail = s.mediaDetail,
            favorite = s.favorite,
            progressMap = s.progressMap,
            danBangumiList = s.danBangumiList,
            danBangumiInfo = s.danBangumiInfo,
            mediaDetailScreenViewModel = mediaDetailScreenViewModel
        )
    }

}