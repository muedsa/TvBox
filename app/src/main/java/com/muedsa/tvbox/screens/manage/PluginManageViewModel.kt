package com.muedsa.tvbox.screens.manage

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.plugin.LoadedPlugins
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.plugin.PluginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PluginManageScreenViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {

    private val internalUiState = MutableSharedFlow<PluginManageUiState>()
    val uiState = internalUiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(60_000),
        initialValue = PluginManageUiState.Loading
    )

    fun refreshPluginInfoList() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = try {
                val loadedPlugins = PluginManager.loadPlugins(context)
                PluginManageUiState.Ready(
                    loadedPlugins = loadedPlugins,
                    apiVersion = getAppApiVersion()
                )
            } catch (throwable: Throwable) {
                Timber.e("加载插件列表失败", throwable)
                PluginManageUiState.Error(throwable.message ?: "加载插件列表失败", throwable)
            }
            withContext(Dispatchers.Main) {
                internalUiState.emit(state)
            }
        }
    }

    private fun getAppApiVersion(): Int {
        return context.packageManager.getApplicationInfo(
            context.applicationInfo.packageName,
            PackageManager.GET_META_DATA
        ).metaData.getInt("tv_box_plugin_api_version", -1)
    }

    fun launchPlugin(
        pluginInfo: PluginInfo,
        onSuccess: () -> Unit,
        onFailure: (e: Throwable) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                PluginManager.launchPlugin(context, pluginInfo)
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure(it)
            }
        }
    }

    private val _downloadPluginFilesSF = MutableStateFlow<List<File>>(emptyList())
    val downloadDir: File =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val downloadPluginFilesSF = _downloadPluginFilesSF.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(60_000),
        initialValue = emptyList()
    )

    fun loadDownloadPluginFiles() {
        viewModelScope.launch {
            val files = downloadDir.listFiles()
                ?.filter { it.isFile && it.canRead() && it.name.endsWith(PluginManager.PLUGIN_FILE_SUFFIX) }
                ?: emptyList()
            _downloadPluginFilesSF.emit(files)
        }
    }

    fun installPlugin(file: File, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                PluginManager.installPlugin(context, file)
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                withContext(Dispatchers.Main) { onFailure(throwable) }
            }
        }
    }

    init {
        refreshPluginInfoList()
    }
}

@Immutable
sealed interface PluginManageUiState {
    data object Loading : PluginManageUiState
    data class Error(val error: String, val exception: Throwable? = null) : PluginManageUiState
    data class Ready(val loadedPlugins: LoadedPlugins, val apiVersion: Int) : PluginManageUiState
}