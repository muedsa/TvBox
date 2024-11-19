package com.muedsa.tvbox.plugin

import com.muedsa.tvbox.api.plugin.IPlugin

class Plugin(
    val pluginInfo: PluginInfo,
    val pluginInstance: IPlugin
) {
    val mainScreenService by lazy { pluginInstance.provideMainScreenService() }
    val mediaDetailService by lazy { pluginInstance.provideMediaDetailService() }
    val mediaSearchService by lazy { pluginInstance.provideMediaSearchService() }
    val mediaCatalogService by lazy { pluginInstance.provideMediaCatalogService() }
    val options  by lazy { pluginInstance.options }
}