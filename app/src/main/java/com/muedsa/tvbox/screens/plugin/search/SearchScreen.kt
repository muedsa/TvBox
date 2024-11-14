package com.muedsa.tvbox.screens.plugin.search

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ErrorScreen
import com.muedsa.compose.tv.widget.LoadingScreen

@Composable
fun SearchScreen(
    searchScreenViewModel: SearchScreenViewModel = hiltViewModel()
) {

    val toastController = useLocalToastMsgBoxController()
    val uiState by searchScreenViewModel.uiState.collectAsStateWithLifecycle()

    Column {
        SearchInput(searching = uiState is SearchScreenUiState.Searching) {
            searchScreenViewModel.searchMedia(it)
        }

        when (val s = uiState) {
            is SearchScreenUiState.Searching -> LoadingScreen()

            is SearchScreenUiState.Error -> ErrorScreen(onError = { toastController.error(s.error) })

            is SearchScreenUiState.Done -> SearchResult(s.row)
        }
    }
}