package com.muedsa.tvbox.screens.plugin.home

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
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class PluginHomeViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {

    private val internalUiState = MutableSharedFlow<PluginHomeUiState>()
    val uiState = internalUiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(60_000),
        initialValue = PluginHomeUiState.Loading
    )

    fun refreshRowsData() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = try {
                PluginManager.getCurrentPlugin()
                    .mainScreenService
                    .getRowsData()
                    .let { PluginHomeUiState.Ready(it) }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                PluginHomeUiState.Error(error = throwable.message ?: "error", exception = throwable)
            }
            internalUiState.emit(state)
        }
    }
}

@Immutable
sealed interface PluginHomeUiState {
    data object Loading : PluginHomeUiState
    data class Error(val error: String, val exception: Throwable? = null) : PluginHomeUiState
    data class Ready(val rows: List<MediaCardRow>) : PluginHomeUiState
}