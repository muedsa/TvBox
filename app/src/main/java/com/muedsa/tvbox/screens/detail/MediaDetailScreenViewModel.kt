package com.muedsa.tvbox.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.api.data.MediaDetail
import com.muedsa.tvbox.api.data.MediaEpisode
import com.muedsa.tvbox.api.data.MediaHttpSource
import com.muedsa.tvbox.api.data.MediaPlaySource
import com.muedsa.tvbox.api.data.SavedMediaCard
import com.muedsa.tvbox.api.plugin.PluginOptions
import com.muedsa.tvbox.danmaku.DanmakuService
import com.muedsa.tvbox.model.DanmakuMedia
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.dao.FavoriteMediaDao
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import com.muedsa.tvbox.room.model.FavoriteMediaModel
import com.muedsa.tvbox.screens.NavigationItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MediaDetailScreenViewModel @Inject constructor(
    private val danmakuService: DanmakuService,
    private val favoriteMediaDao: FavoriteMediaDao,
    private val episodeProgressDao: EpisodeProgressDao
) : ViewModel() {

    private val _uiState: MutableStateFlow<MediaDetailScreenUiState> =
        MutableStateFlow(MediaDetailScreenUiState.Loading)
    val uiState: StateFlow<MediaDetailScreenUiState> = _uiState

    private val _refreshMediaDetailFlow = MutableStateFlow<NavigationItems.Detail?>(null)
    private val _refreshFavoriteFlow = MutableStateFlow(0)
    private val _refreshProgressFlow = MutableStateFlow(0)

    private val _mediaDetailFlow = _refreshMediaDetailFlow
        .filterNotNull()
        .map { navItem ->
            try {
                val plugin = PluginManager.getCurrentPlugin()
                val detail = withContext(Dispatchers.IO) {
                    plugin.mediaDetailService.getDetailData(navItem.id, navItem.url)
                }
                DataWrapper.success(Triple(plugin.pluginInfo, plugin.options, detail))
            } catch (throwable: Throwable) {
                DataWrapper.error<Triple<PluginInfo, PluginOptions, MediaDetail>>(throwable)
            }
        }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(60_000)
        )

    private val _favoriteFlow = combine(_refreshFavoriteFlow, _mediaDetailFlow) { _, wrapper ->
        if (wrapper.data != null) {
            val favoriteMedia = favoriteMediaDao.getOneByPluginPackageAndMediaId(
                pluginPackage = wrapper.data.first.packageName,
                mediaId = wrapper.data.third.id
            )
            Pair(wrapper.data, favoriteMedia)
        } else Pair(wrapper.data, null)
    }

    private val _mediaProgressFlow = combine(_refreshProgressFlow, _mediaDetailFlow) { _, wrapper ->
        if (wrapper.data != null && !wrapper.data.third.disableEpisodeProgression) {
            val progressMap = episodeProgressDao.getListByPluginPackageAndMediaId(
                pluginPackage = wrapper.data.first.packageName,
                mediaId = wrapper.data.third.id
            ).associateBy({ it.episodeId }, { it })
            Pair(wrapper.data, progressMap)
        } else {
            Pair(wrapper.data, emptyMap())
        }
    }

    private val _selectedDanmakuProviderFlow = MutableStateFlow<String?>(
        danmakuService.getProviders().firstOrNull()
    )
    val selectedDanmakuProviderFlow: StateFlow<String?> = _selectedDanmakuProviderFlow

    private val _danmakuMediaSearchQueryFlow = MutableStateFlow<String?>(null)

    private val _danmakuMediaListFlow =
        combine(_mediaDetailFlow, _selectedDanmakuProviderFlow, _danmakuMediaSearchQueryFlow) { wrapper, danmakuProvider, searchQuery ->
            if (wrapper.data != null
                && wrapper.data.second.enableDanDanPlaySearch
                && !wrapper.data.third.enableCustomDanmakuList
                && !wrapper.data.third.enableCustomDanmakuFlow
            ) {
                val dp = danmakuProvider ?: danmakuService.getProviders().firstOrNull()
                if (dp != null) {
                    try {
                        val list = danmakuService.searchMedia(
                            provider = dp,
                            keyword = searchQuery ?: wrapper.data.third.title
                        )
                        Pair(wrapper.data, list)
                    } catch (throwable: Throwable) {
                        Timber.e(throwable)
                        Pair(wrapper.data, emptyList())
                    }
                } else {
                    Pair(wrapper.data, emptyList())
                }
            } else Pair(wrapper.data, null)
        }.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    private val _selectedDanmakuMediaSearchFlow = MutableStateFlow<DanmakuMedia?>(null)

    private val _danmakuMediaDetailFlow = combine(
        _danmakuMediaListFlow,
        _selectedDanmakuMediaSearchFlow
    ) { danmakuMediaListPair, selected ->
        val list = danmakuMediaListPair.second
        val danmakuMedia = if (!list.isNullOrEmpty()) {
            selected?.let {
                list.find { it.provider == selected.provider && it.mediaId == selected.mediaId }
            } ?: list.firstOrNull()
        } else null
        val detail = danmakuMedia?.let {
            try {
                danmakuService.getMediaEpisodes(media = danmakuMedia)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                null
            }
        }
        Pair(danmakuMediaListPair.first, detail)
    }

    fun refreshMediaDetail(navItem: NavigationItems.Detail) {
        viewModelScope.launch {
            if (_refreshMediaDetailFlow.value == navItem) {
                _refreshProgressFlow.update { it + 1 }
            } else {
                _uiState.emit(MediaDetailScreenUiState.Loading)
                _refreshMediaDetailFlow.emit(navItem)
            }
        }
    }

    fun favorite(pluginInfo: PluginInfo, favoriteMediaCard: SavedMediaCard, favorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (favorite) {
                favoriteMediaDao.insertAll(
                    FavoriteMediaModel(
                        pluginPackage = pluginInfo.packageName,
                        mediaId = favoriteMediaCard.id,
                        mediaTitle = favoriteMediaCard.title,
                        mediaDetailUrl = favoriteMediaCard.detailUrl,
                        mediaSubTitle = favoriteMediaCard.subTitle,
                        coverImageUrl = favoriteMediaCard.coverImageUrl,
                        cardWidth = favoriteMediaCard.cardWidth,
                        cardHeight = favoriteMediaCard.cardHeight,
                        updateAt = System.currentTimeMillis(),
                    )
                )
            } else {
                favoriteMediaDao.deleteByPluginPackageAndMediaId(
                    pluginPackage =pluginInfo.packageName,
                    mediaId = favoriteMediaCard.id
                )
            }
            _refreshFavoriteFlow.update {
                it + 1
            }
        }
    }

    fun getDanmakuProviders() = danmakuService.getProviders()

    fun changeDanmakuProvider(selected: String) {
        viewModelScope.launch {
            _selectedDanmakuProviderFlow.emit(selected)
        }
    }

    fun changeDanmakuMedia(selected: DanmakuMedia) {
        viewModelScope.launch {
            _selectedDanmakuMediaSearchFlow.emit(selected)
        }
    }

    fun searchDanmakuMedia(query: String) {
        viewModelScope.launch {
            _danmakuMediaSearchQueryFlow.emit(query)
        }
    }

    fun clearProgress(pluginInfo: PluginInfo, mediaDetail: MediaDetail) {
        viewModelScope.launch {
            episodeProgressDao.deleteByPluginPackageAndMediaId(
                pluginPackage = pluginInfo.packageName,
                mediaId = mediaDetail.id
            )
            _refreshProgressFlow.update { it + 1 }
        }
    }

    fun getEpisodePlayInfo(
        playSource: MediaPlaySource,
        episode: MediaEpisode,
        onSuccess: (MediaHttpSource) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val info = PluginManager.getCurrentPlugin()
                    .mediaDetailService
                    .getEpisodePlayInfo(playSource = playSource, episode = episode)
                if (info.url.isBlank()) {
                    throw RuntimeException("获取视频地址失败, 返回视频地址为空")
                }
                withContext(Dispatchers.Main) {
                    onSuccess(info)
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
            combine(
                _mediaDetailFlow,
                _favoriteFlow,
                _mediaProgressFlow,
                _danmakuMediaListFlow,
                _danmakuMediaDetailFlow
            ) { wrapper, favoritePair, progressMapPair, danmakuMediaListPair, danmakuMediaDetailPair ->
                if (progressMapPair.first == null) {
                    Timber.e(wrapper.error, wrapper.error?.message ?: "error")
                    MediaDetailScreenUiState.Error(
                        error = wrapper.error?.message ?: "error",
                        exception = wrapper.error
                    )
                } else if (wrapper.data != null && favoritePair.first == wrapper.data
                    && progressMapPair.first == wrapper.data && danmakuMediaListPair.first == wrapper.data
                    && danmakuMediaDetailPair.first == wrapper.data
                ) {
                    MediaDetailScreenUiState.Ready(
                        pluginInfo = wrapper.data.first,
                        mediaDetail = wrapper.data.third,
                        favorite = favoritePair.second != null,
                        progressMap = progressMapPair.second,
                        danmakuProviders = danmakuService.getProviders(),
                        danmakuMediaList = danmakuMediaListPair.second,
                        danmakuMediaInfo = danmakuMediaDetailPair.second
                    )
                } else null
            }.filterNotNull().collect {
                _uiState.emit(it)
            }
        }
    }
}

sealed interface MediaDetailScreenUiState {
    data object Loading : MediaDetailScreenUiState
    data class Error(val error: String, val exception: Throwable? = null) : MediaDetailScreenUiState
    data class Ready(
        val pluginInfo: PluginInfo,
        val mediaDetail: MediaDetail,
        val favorite: Boolean,
        val progressMap: Map<String, EpisodeProgressModel>,
        val danmakuProviders: Set<String>,
        val danmakuMediaList: List<DanmakuMedia>?, // 为null表示插件禁用了弹幕搜索
        val danmakuMediaInfo: DanmakuMedia?,
    ) : MediaDetailScreenUiState
}

data class DataWrapper<T>(
    val data: T? = null,
    val error: Throwable? = null
) {
    companion object {
        fun <T> success(data: T): DataWrapper<T> = DataWrapper<T>(data = data)
        fun <T> error(error: Throwable): DataWrapper<T> = DataWrapper<T>(error = error)
    }
}
