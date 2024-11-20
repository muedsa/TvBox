package com.muedsa.tvbox.screens.plugin.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muedsa.compose.tv.useLocalToastMsgBoxController
import com.muedsa.compose.tv.widget.ErrorScreen
import com.muedsa.compose.tv.widget.LoadingScreen

@Composable
fun CatalogScreen(
    catalogScreenViewModel: CatalogScreenViewModel
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
            plugin = s.plugin,
            config = s.config,
            catalogScreenViewModel = catalogScreenViewModel
        )
    }
}