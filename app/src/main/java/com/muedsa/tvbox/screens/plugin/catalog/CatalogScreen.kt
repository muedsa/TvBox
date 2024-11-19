package com.muedsa.tvbox.screens.plugin.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ErrorScreen
import com.muedsa.compose.tv.widget.LoadingScreen

@Composable
fun CatalogScreen(
    catalogScreenViewModel: CatalogScreenViewModel = hiltViewModel()
) {
    val toastController = useLocalToastMsgBoxController()
    val uiState by catalogScreenViewModel.catalogConfigUIState.collectAsStateWithLifecycle()
    when (val s = uiState) {
        is CatalogScreenUIState.Loading -> LoadingScreen()

        is CatalogScreenUIState.Error -> ErrorScreen(
            onError = {
                toastController.error(s.error)
            },
            onRefresh = { catalogScreenViewModel.refreshConfig() }
        )

        is CatalogScreenUIState.Ready -> CatalogWidget(
            config = s.config,
            catalogScreenViewModel = catalogScreenViewModel
        )
    }
}