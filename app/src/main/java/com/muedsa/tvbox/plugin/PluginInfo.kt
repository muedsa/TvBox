package com.muedsa.tvbox.plugin

import android.graphics.drawable.Drawable

open class PluginInfo(
    var apiVersion: Int,
    var entryPointImpl: String,
    var packageName: String,
    var name: String,
    var versionName: String,
    var versionCode: Long,
    var icon: Drawable,
    var sourcePath: String,
    var isExternalPlugin: Boolean = false
)