package com.muedsa.tvbox.screens.plugin.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.muedsa.tvbox.api.data.MediaCatalogConfig
import com.muedsa.tvbox.api.data.MediaCatalogOption
import com.muedsa.tvbox.api.service.IMediaCatalogService
import com.muedsa.tvbox.plugin.Plugin
import com.muedsa.tvbox.plugin.PluginManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogScreenViewModel @Inject constructor() : ViewModel() {

    private val _refreshFlow = MutableStateFlow(0)
    private val _catalogConfigUIState =
        MutableStateFlow<CatalogScreenUIState>(CatalogScreenUIState.Loading)
    val catalogConfigUIState: StateFlow<CatalogScreenUIState> = _catalogConfigUIState

    private val _selectedOptionsFlow = MutableStateFlow<List<MediaCatalogOption>>(emptyList())
    val selectedOptionsFlow = _selectedOptionsFlow

    val pageDataFlow = combine(
        _catalogConfigUIState
            .filterIsInstance<CatalogScreenUIState.Ready>()
            .distinctUntilChanged(),
        _selectedOptionsFlow,
        ::Pair
    ).flatMapLatest {
        catalog(
            service = it.first.plugin.mediaCatalogService,
            config = it.first.config,
            selectedOptions = it.second
        )
    }.cachedIn(viewModelScope)

    fun changeSelectedOptions(selectedOptions: List<MediaCatalogOption>) {
        viewModelScope.launch {
            selectedOptionsFlow.emit(selectedOptions)
        }
    }

    private fun catalog(
        service: IMediaCatalogService,
        config: MediaCatalogConfig,
        selectedOptions: List<MediaCatalogOption>
    ) = Pager(
        config = PagingConfig(
            pageSize = config.pageSize,
            enablePlaceholders = false,
            initialLoadSize = config.pageSize,
        ),
        pagingSourceFactory = {
            MediaCatalogPagingSource(
                config = config,
                service = service,
                options = selectedOptions,
            )
        }
    ).flow.cachedIn(viewModelScope)

    fun refreshConfig() {
        viewModelScope.launch {
            _catalogConfigUIState.emit(CatalogScreenUIState.Loading)
            _refreshFlow.update { it + 1 }
        }
    }

    init {
        viewModelScope.launch {
            combine(_refreshFlow, PluginManager.pluginFlow) { _, plugin ->
                if (plugin != null) {
                    try {
                        withContext(Dispatchers.IO) {
                            plugin.mediaCatalogService.getConfig()
                        }.let {

                            CatalogScreenUIState.Ready(plugin = plugin, config = it)
                        }
                    } catch (throwable: Throwable) {
                        Timber.e(throwable, "mediaCatalogService.getConfig error")
                        CatalogScreenUIState.Error(throwable.message ?: "error", throwable)
                    }
                } else CatalogScreenUIState.Loading
            }.catch {
                CatalogScreenUIState.Error(it.message ?: "error", it)
            }.onEach {
                if (it is CatalogScreenUIState.Ready) {
                    _selectedOptionsFlow.emit(MediaCatalogOption.getDefault(it.config.catalogOptions))
                }
            }.collect { _catalogConfigUIState.emit(it) }
        }
    }
}

sealed interface CatalogScreenUIState {
    data object Loading : CatalogScreenUIState
    data class Error(val error: String, val exception: Throwable? = null) : CatalogScreenUIState
    data class Ready(val plugin: Plugin, val config: MediaCatalogConfig) :
        CatalogScreenUIState
}