package com.muedsa.tvbox.screens.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.muedsa.tvbox.model.AppSettingModel
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.service.DanDanPlayApiService
import com.muedsa.tvbox.store.DataStoreRepo
import com.muedsa.tvbox.tool.LenientJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaybackScreenViewModel @Inject constructor(
    private val danDanPlayApiService: DanDanPlayApiService,
    dateStoreRepo: DataStoreRepo,
    private val episodeProgressDao: EpisodeProgressDao
) : ViewModel() {

    private val initFlow = MutableStateFlow<NavigationItems.Player?>(null)
    private val navItemFlow = initFlow.filterNotNull()
    private val episodeProgressFlow = navItemFlow
        .map {
            Timber.d("loading episode progress <${it.pluginPackage}> $${it.mediaId} -> $${it.episodeId}")
            episodeProgressDao.getOneByPluginPackageAndMediaIdAndEpisodeId(
                pluginPackage = it.pluginPackage,
                mediaId = it.mediaId,
                episodeId = it.episodeId
            ) ?: EpisodeProgressModel(it.pluginPackage, it.mediaId, it.episodeId, 0, 0, 0)
        }

    private val _danmakuListFlow = navItemFlow
        .map { it ->
            if (it.danEpisodeId > 0) {
                    danDanPlayApiService.getComment(
                        episodeId = it.danEpisodeId,
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
        navItemFlow,
        episodeProgressFlow,
        _danmakuListFlow,
        dateStoreRepo.dataStore.data.map { AppSettingModel.fromPreferences(it) },
    ) { param, episodeProgress, danmakuList, appSetting ->
        PlayBackScreenUiState.Ready(
            url = param.url,
            httpHeaders = param.httpHeadersJson?.let {
                LenientJson.decodeFromString<Map<String, String>>(
                    it
                )
            },
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

    fun load(navItem: NavigationItems.Player) {
        viewModelScope.launch {
            initFlow.emit(navItem)
        }
    }

    fun saveEpisodeProgress(model: EpisodeProgressModel) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("save episode progress: ${model.progress}/${model.duration}")
            episodeProgressDao.upsert(model)
        }
    }
}

sealed interface PlayBackScreenUiState {
    data object Loading : PlayBackScreenUiState
    data class Error(val error: String, val exception: Throwable? = null) : PlayBackScreenUiState
    data class Ready(
        val url: String,
        val httpHeaders: Map<String, String>?,
        val episodeProgress: EpisodeProgressModel,
        val danmakuList: List<DanmakuItemData>,
        val appSetting: AppSettingModel
    ) : PlayBackScreenUiState
}