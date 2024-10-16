package com.muedsa.tvbox.screens.plugin.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.room.dao.FavoriteMediaDao
import com.muedsa.tvbox.room.model.FavoriteMediaModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteMediaScreenViewModel @Inject constructor(
    private val dao: FavoriteMediaDao
) : ViewModel() {

    val favoriteMediasSF = dao.flowByPluginPackage(
        pluginPackage = PluginManager.getCurrentPlugin().pluginInfo.packageName
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun remove(model: FavoriteMediaModel) {
        viewModelScope.launch {
            dao.deleteByPluginPackageAndMediaId(
                pluginPackage = model.pluginPackage,
                mediaId = model.mediaId
            )
        }
    }
}