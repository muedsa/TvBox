package com.muedsa.tvbox.screens.manage

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.plugin.LoadedPlugins
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.dao.FavoriteMediaDao
import com.muedsa.tvbox.store.DataStoreRepo
import com.muedsa.tvbox.store.PluginKeyCache
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PluginManageScreenViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val favoriteMediaDao: FavoriteMediaDao,
    private val episodeProgressDao: EpisodeProgressDao,
    private val dateStoreRepo: DataStoreRepo,
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
                Timber.e(throwable, "加载插件列表失败")
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
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                PluginManager.launchPlugin(
                    context = context,
                    pluginInfo = pluginInfo,
                    pluginDataStore = dateStoreRepo.pluginDataStore
                )
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    onFailure(it)
                }
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

    fun uninstallPlugin(pluginInfo: PluginInfo, onSuccess: () -> Unit, onFailure: (Throwable?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (PluginManager.uninstallPlugin(pluginInfo)) {
                    favoriteMediaDao.deleteByPluginPackage(pluginPackage = pluginInfo.packageName)
                    episodeProgressDao.deleteByPluginPackage(pluginPackage = pluginInfo.packageName)
                    dateStoreRepo.pluginDataStore.edit { prefs ->
                        val keys = prefs.asMap().keys
                        keys.forEach { key ->
                            if (key.name.startsWith(PluginKeyCache.getGlobalKeyPrefix(pluginPackage = pluginInfo.packageName))) {
                                prefs.remove(key)
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure(null)
                    }
                }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                withContext(Dispatchers.Main) {
                    onFailure(throwable)
                }
            }
        }
    }

    fun deleteFile(file: File, onSuccess: () -> Unit, onFailure: (Throwable?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (file.deleteRecursively()) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFailure(null)
                    }
                }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                withContext(Dispatchers.Main) {
                    onFailure(throwable)
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            var i = 0
            while (i < 5 * 120) {
                delay(200.milliseconds)
                if (PluginManager.isInit()) {
                    refreshPluginInfoList()
                    break
                }
                i++
            }
            if (i == 5 * 120) {
                internalUiState.emit(PluginManageUiState.Error("初始化插件管理器超时"))
            }
        }
    }
}

@Immutable
sealed interface PluginManageUiState {
    data object Loading : PluginManageUiState
    data class Error(val error: String, val exception: Throwable? = null) : PluginManageUiState
    data class Ready(val loadedPlugins: LoadedPlugins, val apiVersion: Int) : PluginManageUiState
}