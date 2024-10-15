package com.muedsa.tvbox.screens.plugin.search

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.plugin.PluginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchScreenViewModel  @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {

    private val internalUiState = MutableSharedFlow<SearchScreenUiState>()
    val uiState = internalUiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchScreenUiState.Done(MediaCardRow("empty", emptyList()))
    )

    fun searchMedia(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                internalUiState.emit(SearchScreenUiState.Searching)
                val state = try {
                    PluginManager.getCurrentPlugin()
                        .mediaSearchService
                        .searchMedias(query)
                        .let { SearchScreenUiState.Done(it) }
                } catch (throwable: Throwable) {
                    SearchScreenUiState.Error(throwable.message ?: "error", throwable)
                }
                internalUiState.emit(state)
            }
        }
    }
}

@Immutable
sealed interface SearchScreenUiState {
    data object Searching : SearchScreenUiState
    data class Error(val error: String, val exception: Throwable? = null) : SearchScreenUiState
    data class Done(val row: MediaCardRow) : SearchScreenUiState
}