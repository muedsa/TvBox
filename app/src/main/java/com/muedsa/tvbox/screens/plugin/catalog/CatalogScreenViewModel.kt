package com.muedsa.tvbox.screens.plugin.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCatalogConfig
import com.muedsa.tvbox.api.data.MediaCatalogOption
import com.muedsa.tvbox.plugin.PluginManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CatalogScreenViewModel @Inject constructor() : ViewModel() {

    private val _catalogConfigUIState =
        MutableStateFlow<CatalogScreenUIState>(CatalogScreenUIState.Loading)
    val catalogConfigUIState: StateFlow<CatalogScreenUIState> = _catalogConfigUIState

    fun newPager(
        config: MediaCatalogConfig,
        selectedOptions: List<MediaCatalogOption>
    ): Pager<String, MediaCard> {
        return Pager(
            config = PagingConfig(
                pageSize = config.pageSize,
                enablePlaceholders = false,
                initialLoadSize = config.pageSize,
            ),
            pagingSourceFactory = {
                MediaCatalogPagingSource(
                    config = config,
                    service = PluginManager.getCurrentPlugin().mediaCatalogService,
                    options = selectedOptions,
                )
            }
        )
    }

    fun refreshConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            _catalogConfigUIState.emit(CatalogScreenUIState.Loading)
            val state = try {
                val config = PluginManager.getCurrentPlugin().mediaCatalogService
                    .getConfig()
                CatalogScreenUIState.Ready(config)
            } catch (throwable: Throwable) {
                Timber.e(throwable, "mediaCatalogService.getConfig error")
                CatalogScreenUIState.Error(throwable.message ?: "error", throwable)
            }
            _catalogConfigUIState.emit(state)
        }
    }

    init {
        refreshConfig()
    }
}

sealed interface CatalogScreenUIState {
    data object Loading : CatalogScreenUIState
    data class Error(val error: String, val exception: Throwable? = null) : CatalogScreenUIState
    data class Ready(val config: MediaCatalogConfig) : CatalogScreenUIState
}