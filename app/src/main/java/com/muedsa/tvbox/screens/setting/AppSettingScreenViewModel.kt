package com.muedsa.tvbox.screens.setting

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.model.AppSettingModel
import com.muedsa.tvbox.store.DataStoreRepo
import com.muedsa.tvbox.store.KEY_DANMAKU_ALPHA
import com.muedsa.tvbox.store.KEY_DANMAKU_ENABLE
import com.muedsa.tvbox.store.KEY_DANMAKU_MERGE_ENABLE
import com.muedsa.tvbox.store.KEY_DANMAKU_SCREEN_PART
import com.muedsa.tvbox.store.KEY_DANMAKU_SIZE_SCALE
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
    private val repo: DataStoreRepo
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
}