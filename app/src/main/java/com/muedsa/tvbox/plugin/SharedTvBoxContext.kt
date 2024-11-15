package com.muedsa.tvbox.plugin

import com.muedsa.tvbox.api.plugin.TvBoxContext
import com.muedsa.tvbox.api.store.IPluginPerfStore
import com.muedsa.tvbox.tool.IPv6Checker

data class SharedTvBoxContext(
    val screenWidth: Int,                       // TV屏幕宽度dp
    val screenHeight: Int,                      // TV屏幕高度dp
    val debug: Boolean,                         // debug模式
    var iPv6Status: IPv6Checker.IPv6Status      // IPv6状态
) {
    fun createTvBoxContext(store: IPluginPerfStore): TvBoxContext = TvBoxContext(
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        debug = debug,
        store = store,
        iPv6Status = iPv6Status,
    )
}
