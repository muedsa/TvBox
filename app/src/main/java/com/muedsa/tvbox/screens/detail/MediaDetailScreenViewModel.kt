package com.muedsa.tvbox.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.api.data.MediaDetail
import com.muedsa.tvbox.api.data.MediaEpisode
import com.muedsa.tvbox.api.data.MediaHttpSource
import com.muedsa.tvbox.api.data.MediaPlaySource
import com.muedsa.tvbox.api.data.SavedMediaCard
import com.muedsa.tvbox.model.dandanplay.BangumiInfo
import com.muedsa.tvbox.model.dandanplay.BangumiSearch
import com.muedsa.tvbox.plugin.PluginInfo
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.room.dao.EpisodeProgressDao
import com.muedsa.tvbox.room.dao.FavoriteMediaDao
import com.muedsa.tvbox.room.model.EpisodeProgressModel
import com.muedsa.tvbox.room.model.FavoriteMediaModel
import com.muedsa.tvbox.screens.NavigationItems
import com.muedsa.tvbox.service.DanDanPlayApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MediaDetailScreenViewModel @Inject constructor(
    private val danDanPlayApiService: DanDanPlayApiService,
    private val favoriteMediaDao: FavoriteMediaDao,
    private val episodeProgressDao: EpisodeProgressDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _refreshFavoriteFlow = MutableStateFlow(0)
    private val _refreshProgressListFlow = MutableStateFlow(0)

    private val _mediaDetailFlow = combine(
        savedStateHandle.getStateFlow<String?>(MEDIA_ID_SAVED_STATE_KEY, null).filterNotNull(),
        savedStateHandle.getStateFlow<String?>(MEDIA_URL_SAVED_STATE_KEY, null).filterNotNull()
    ) { id, url ->
        val plugin = PluginManager.getCurrentPlugin()
        Triple(plugin.pluginInfo, plugin.options, plugin.mediaDetailService.getDetailData(id, url))
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(60_000)
    )

    private val _favoriteFlow = combine(_refreshFavoriteFlow, _mediaDetailFlow) { _, pd ->
        favoriteMediaDao.getOneByPluginPackageAndMediaId(
            pluginPackage = pd.first.packageName,
            mediaId = pd.third.id
        )
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    private val _mediaProgressListFlow =
        combine(_refreshProgressListFlow, _mediaDetailFlow) { _, pd ->
            Timber.d("load media progress <${pd.first.packageName}> ${pd.third.id}")
            episodeProgressDao.getListByPluginPackageAndMediaId(
                pluginPackage = pd.first.packageName,
                mediaId = pd.third.id
            )
        }.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    private val _banBangumiSearchQueryFlow = MutableStateFlow<String?>(null)

    private val _danBangumiListFlow = combine(_mediaDetailFlow, _banBangumiSearchQueryFlow) { pd, searchQuery ->
        if (pd.second.enableDanDanPlaySearch) {
            try {
                val resp = danDanPlayApiService.searchAnime(searchQuery ?: pd.third.title)
                if (resp.errorCode == 0) {
                    resp.animes ?: emptyList()
                } else {
                    Timber.d("danDanPlayApiService.searchAnime(${pd.third.title}) ${resp.errorMessage}")
                    emptyList()
                }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                emptyList()
            }
        } else null
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    private val _selectedDanBangumiSearchFlow = MutableStateFlow<BangumiSearch?>(null)

    private val _danBangumiDetailFlow = combine(_danBangumiListFlow, _selectedDanBangumiSearchFlow) { list, selected ->
        val anime = if (!list.isNullOrEmpty()){
            selected?.let { list.find { it.animeId == selected.animeId } } ?: list.firstOrNull()
        } else null
        if (anime != null) {
            try {
                val resp = danDanPlayApiService.getAnime(anime.animeId)
                if (resp.errorCode == 0) {
                    resp.bangumi
                } else {
                    Timber.d("danDanPlayApiService.getAnime(${anime.animeId}) ${resp.errorMessage}")
                    null
                }
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                null
            }
        } else null
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val uiState: StateFlow<MediaDetailScreenUiState> = combine(
        _mediaDetailFlow,
        _favoriteFlow,
        _mediaProgressListFlow,
        _danBangumiListFlow,
        _danBangumiDetailFlow
    ) { pd, favorite, progressList, danBangumiList, danBangumiInfo ->
        MediaDetailScreenUiState.Ready(
            pluginInfo = pd.first,
            mediaDetail = pd.third,
            favorite = favorite != null,
            progressMap = progressList.associateBy({ it.episodeId }, { it }),
            danBangumiList = danBangumiList,
            danBangumiInfo = danBangumiInfo
        )
    }.catch {
        MediaDetailScreenUiState.Error(error = it.message ?: "error", exception = it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(60_000),
        initialValue = MediaDetailScreenUiState.Loading
    )

    fun refreshProgressList() {
        viewModelScope.launch {
            _refreshProgressListFlow.update {
                it + 1
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
                        coverImageUrl = favoriteMediaCard.coverImageUrl,
                        cardWidth = favoriteMediaCard.cardWidth,
                        cardHeight = favoriteMediaCard.cardHeight
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

    fun changeDanBangumi(selected: BangumiSearch) {
        viewModelScope.launch {
            _selectedDanBangumiSearchFlow.emit(selected)
        }
    }

    fun searchDanBangumi(query: String) {
        viewModelScope.launch {
            _banBangumiSearchQueryFlow.emit(query)
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

    companion object {
        val MEDIA_ID_SAVED_STATE_KEY: String = NavigationItems.Detail.args[0].name
        val MEDIA_URL_SAVED_STATE_KEY: String = NavigationItems.Detail.args[1].name
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
        val danBangumiList: List<BangumiSearch>?, // 为null表示插件不支持dandanplay
        val danBangumiInfo: BangumiInfo?,
    ) :
        MediaDetailScreenUiState
}
