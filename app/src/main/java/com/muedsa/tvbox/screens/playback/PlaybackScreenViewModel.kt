package com.muedsa.tvbox.screens.playback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.model.AppSettingModel
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.service.DanDanPlayApiService
import com.muedsa.tvbox.store.DataStoreRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaybackScreenViewModel @Inject constructor(
    private val danDanPlayApiService: DanDanPlayApiService,
    dateStoreRepo: DataStoreRepo,
    private val episodeProgressDao: EpisodeProgressDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val episodeProgressFlow = combine(
        savedStateHandle.getStateFlow(PLUGIN_PACKAGE_SAVED_STATE_KEY, ""),
        savedStateHandle.getStateFlow(MEDIA_ID_SAVED_STATE_KEY, ""),
        savedStateHandle.getStateFlow(EPISODE_ID_SAVED_STATE_KEY, "")
    ) { pluginPackage, mediaId, episodeId ->
        Timber.d("loading episode progress <$pluginPackage> $mediaId -> $episodeId")
        episodeProgressDao.getOneByPluginPackageAndMediaIdAndEpisodeId(
            pluginPackage = pluginPackage,
            mediaId = mediaId,
            episodeId = episodeId
        ) ?: EpisodeProgressModel(pluginPackage, mediaId, episodeId, 0, 0, 0)
    }

    private val _danmakuListFlow =
        savedStateHandle.getStateFlow(DAN_EPISODE_ID_SAVED_STATE_KEY, -1L)
            .map { danEpisodeId ->
                if (danEpisodeId > 0) {
                    danDanPlayApiService.getComment(
                        episodeId = danEpisodeId,
                        from = 0,
                        withRelated = true,
                        chConvert = 1
                    ).comments.map {
                        val propArr = it.p.split(",")
                        val pos = (propArr[0].toFloat() * 1000).toLong()
                        val mode = if (propArr[1] == "1")
                            DanmakuItemData.DANMAKU_MODE_ROLLING
                        else if (propArr[1] == "4")
                            DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM
                        else if (propArr[1] == "5")
                            DanmakuItemData.DANMAKU_MODE_CENTER_TOP
                        else
                            DanmakuItemData.DANMAKU_MODE_ROLLING
                        val colorInt = propArr[2].toInt()
                        DanmakuItemData(
                            danmakuId = it.cid,
                            position = pos,
                            content = it.m,
                            mode = mode,
                            textSize = 25,
                            textColor = colorInt,
                            score = 9,
                            danmakuStyle = DanmakuItemData.DANMAKU_STYLE_NONE
                        )
                    }
                } else emptyList()
            }

    val uiState = combine(
        savedStateHandle.getStateFlow(URL_SAVED_STATE_KEY, ""),
        episodeProgressFlow,
        _danmakuListFlow,
        dateStoreRepo.dataStore.data.map { AppSettingModel.fromPreferences(it) },
    ) { url, episodeProgress, danmakuList, appSetting ->
        PlayBackScreenUiState.Ready(
            url = url,
            episodeProgress = episodeProgress,
            danmakuList = danmakuList,
            appSetting = appSetting
        )
    }.catch {
        PlayBackScreenUiState.Error(error = it.message ?: "error", it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(60_000),
        initialValue = PlayBackScreenUiState.Loading
    )

    fun saveEpisodeProgress(model: EpisodeProgressModel) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("save episode progress: ${model.progress}/${model.duration}")
            episodeProgressDao.upsert(model)
        }
    }

    companion object {
        val URL_SAVED_STATE_KEY: String = NavigationItems.Player.args[0].name
        val PLUGIN_PACKAGE_SAVED_STATE_KEY: String = NavigationItems.Player.args[1].name
        val MEDIA_ID_SAVED_STATE_KEY: String = NavigationItems.Player.args[2].name
        val EPISODE_ID_SAVED_STATE_KEY: String = NavigationItems.Player.args[3].name
        val DAN_EPISODE_ID_SAVED_STATE_KEY: String = NavigationItems.Player.args[4].name
    }
}

sealed interface PlayBackScreenUiState {
    data object Loading : PlayBackScreenUiState
    data class Error(val error: String, val exception: Throwable? = null) : PlayBackScreenUiState
    data class Ready(
        val url: String,
        val episodeProgress: EpisodeProgressModel,
        val danmakuList: List<DanmakuItemData>,
        val appSetting: AppSettingModel
    ) : PlayBackScreenUiState
}