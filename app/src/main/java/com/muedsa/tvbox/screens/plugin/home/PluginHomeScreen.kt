package com.muedsa.tvbox.screens.plugin.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ErrorScreen
import com.muedsa.compose.tv.widget.LoadingScreen

@Composable
fun PluginHomeScreen(
    pluginHomeViewModel: PluginHomeViewModel = hiltViewModel()
) {
    val toastController = useLocalToastMsgBoxController()
    val uiState by pluginHomeViewModel.uiState.collectAsState()

    when (val s = uiState) {
        is PluginHomeUiState.Loading -> LoadingScreen()

        is PluginHomeUiState.Error -> ErrorScreen(
            onError = {
                toastController.error(s.error)
            },
            onRefresh = { pluginHomeViewModel.refreshRowsData() }
        )

        is PluginHomeUiState.Ready -> MediaCardRows(pluginInfo = s.pluginInfo, rows = s.rows)
    }
}



