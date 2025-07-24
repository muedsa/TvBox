package com.muedsa.tvbox.screens.plugin.home

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.api.data.MediaCardRow
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.plugin.PluginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class PluginHomeViewModel @Inject constructor(
    @param:ApplicationContext val context: Context
) : ViewModel() {

    private val _refreshFlow = MutableStateFlow(0)
    private val _uiState: MutableStateFlow<PluginHomeUiState> =
        MutableStateFlow(PluginHomeUiState.Loading)
    val uiState: StateFlow<PluginHomeUiState> = _uiState

    fun refreshRowsData() {
        viewModelScope.launch {
            _uiState.emit(PluginHomeUiState.Loading)
            _refreshFlow.update { it + 1 }
        }
    }

    init {
        viewModelScope.launch {
            combine(_refreshFlow, PluginManager.pluginFlow) { count, plugin ->
                if (plugin != null) {
                    try {
                        withContext(Dispatchers.IO) {
                            plugin.mainScreenService.getRowsData()
                        }.let { PluginHomeUiState.Ready(pluginInfo = plugin.pluginInfo, rows = it) }
                    } catch (throwable: Throwable) {
                        Timber.e(throwable)
                        PluginHomeUiState.Error(
                            error = throwable.message ?: "error",
                            exception = throwable
                        )
                    }
                } else PluginHomeUiState.Loading as PluginHomeUiState
            }.retryWhen { cause, attempt ->
                cause is IOException && attempt < 3
            }.catch {
                // 为什么到这里的异常会停止combine的收集?
                Timber.e(it, "combine.catch")
                emit(PluginHomeUiState.Error(error = it.message ?: "error", exception = it))
            }.collect { _uiState.emit(it) }
        }
    }
}

@Immutable
sealed interface PluginHomeUiState {
    data object Loading : PluginHomeUiState
    data class Error(val error: String, val exception: Throwable? = null) : PluginHomeUiState
    data class Ready(val pluginInfo: PluginInfo, val rows: List<MediaCardRow>) : PluginHomeUiState
}