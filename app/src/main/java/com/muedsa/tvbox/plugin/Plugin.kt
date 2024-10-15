package com.muedsa.tvbox.plugin

import com.muedsa.tvbox.api.plugin.IPlugin

class Plugin(
    val pluginInfo: PluginInfo,
    val pluginInstance: IPlugin
) {
    val mainScreenService = pluginInstance.provideMainScreenService()
    val mediaDetailService = pluginInstance.provideMediaDetailService()
    val mediaSearchService = pluginInstance.provideMediaSearchService()
    val options = pluginInstance.options
}