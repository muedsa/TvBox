package com.muedsa.tvbox.screens.plugin.search

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.screens.plugin.catalog.CatalogScreenUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchScreenViewModel  @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {

    private val _queryFlow = MutableStateFlow("")
    private val _uiState = MutableStateFlow<SearchScreenUiState>(SearchScreenUiState.Searching)
    val uiState: StateFlow<SearchScreenUiState> = _uiState

    fun searchMedia(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                _queryFlow.emit(query)
            }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                PluginManager.pluginFlow.filterNotNull(),
                _queryFlow
            ) { plugin, query ->
                try {
                    val row = if (query.isNotBlank()) {
                        plugin.mediaSearchService
                            .searchMedias(query)
                    } else EMPTY_ROW
                    SearchScreenUiState.Done(
                        pluginInfo = plugin.pluginInfo,
                        row = row
                    )
                } catch (throwable: Throwable) {
                    SearchScreenUiState.Error(throwable.message ?: "error", throwable)
                }
            }.catch {
                CatalogScreenUIState.Error(it.message ?: "error", it)
            }.collect { _uiState.emit(it) }
        }
    }

    companion object {
        val EMPTY_ROW = MediaCardRow("empty", emptyList())
    }
}

@Immutable
sealed interface SearchScreenUiState {
    data object Searching : SearchScreenUiState
    data class Error(val error: String, val exception: Throwable? = null) : SearchScreenUiState
    data class Done(val pluginInfo: PluginInfo, val row: MediaCardRow) : SearchScreenUiState
}