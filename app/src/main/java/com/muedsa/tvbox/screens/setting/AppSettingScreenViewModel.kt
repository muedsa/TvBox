package com.muedsa.tvbox.screens.setting

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.model.AppSettingModel
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.dao.FavoriteMediaDao
import com.muedsa.tvbox.store.DataStoreRepo
import com.muedsa.tvbox.store.KEY_DANMAKU_ALPHA
import com.muedsa.tvbox.store.KEY_DANMAKU_ENABLE
import com.muedsa.tvbox.store.KEY_DANMAKU_MERGE_ENABLE
import com.muedsa.tvbox.store.KEY_DANMAKU_SCREEN_PART
import com.muedsa.tvbox.store.KEY_DANMAKU_SIZE_SCALE
import com.muedsa.tvbox.store.KEY_FSR_ENABLE
import com.muedsa.tvbox.store.PluginKeyCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingScreenViewModel @Inject constructor(
    private val repo: DataStoreRepo,
    private val favoriteMediaDao: FavoriteMediaDao,
    private val episodeProgressDao: EpisodeProgressDao,
) : ViewModel() {

    val settingSF: StateFlow<AppSettingModel> = repo.dataStore.data
        .map { AppSettingModel.fromPreferences(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettingModel()
        )

    fun changeDanmakuEnable(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.dataStore.edit {
                it[KEY_DANMAKU_ENABLE] = enable
            }
        }
    }

    fun changeDanmakuMergeEnable(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.dataStore.edit {
                it[KEY_DANMAKU_MERGE_ENABLE] = enable
            }
        }
    }

    fun changeDanmakuSizeScale(value: Int) {
        if (value in 10..300) {
            viewModelScope.launch(Dispatchers.IO) {
                repo.dataStore.edit {
                    it[KEY_DANMAKU_SIZE_SCALE] = value
                }
            }
        }
    }

    fun changeDanmakuAlpha(value: Int) {
        if (value in 0..100) {
            viewModelScope.launch(Dispatchers.IO) {
                repo.dataStore.edit {
                    it[KEY_DANMAKU_ALPHA] = value
                }
            }
        }
    }

    fun changeDanmakuScreenPart(value: Int) {
        if (value in 10..100) {
            viewModelScope.launch(Dispatchers.IO) {
                repo.dataStore.edit {
                    it[KEY_DANMAKU_SCREEN_PART] = value
                }
            }
        }
    }

    fun clearPluginFavoriteMedias() {
        viewModelScope.launch(Dispatchers.IO) {
            val pluginPackage = PluginManager.getCurrentPlugin().pluginInfo.packageName
            favoriteMediaDao.deleteByPluginPackage(pluginPackage = pluginPackage)
        }
    }

    fun clearPluginEpisodeProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            val pluginPackage = PluginManager.getCurrentPlugin().pluginInfo.packageName
            episodeProgressDao.deleteByPluginPackage(pluginPackage = pluginPackage)
        }
    }

    fun clearPluginDataStore() {
        viewModelScope.launch(Dispatchers.IO) {
            val pluginPackage = PluginManager.getCurrentPlugin().pluginInfo.packageName
            val pluginKeyPrefix = PluginKeyCache.getGlobalKeyPrefix(pluginPackage)
            repo.pluginDataStore.edit {
                val map = it.asMap()
                for ((key) in map) {
                    if (key.name.startsWith(pluginKeyPrefix)) {
                        it.remove(key)
                    }
                }
            }
        }
    }

    fun changeFsrEnable(enable: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.dataStore.edit {
                it[KEY_FSR_ENABLE] = enable
            }
        }
    }
}