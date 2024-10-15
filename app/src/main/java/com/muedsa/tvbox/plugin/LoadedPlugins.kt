package com.muedsa.tvbox.plugin

import java.io.File

data class LoadedPlugins(
    val plugins: List<PluginInfo>,
    val invalidFiles: List<File>
)
