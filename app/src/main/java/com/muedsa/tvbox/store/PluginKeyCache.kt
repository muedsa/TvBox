package com.muedsa.tvbox.store

import androidx.datastore.preferences.core.Preferences
import com.muedsa.tvbox.api.store.PluginPerfKey

object PluginKeyCache {
    private val cache: MutableMap<String, Preferences.Key<*>> = mutableMapOf()

    private const val GLOBAL_KEY_NAME_SEPARATOR = ":"

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T> getGlobalKey(pluginPackage: String, key: PluginPerfKey<T>): Preferences.Key<T> {
        val name = "$pluginPackage$GLOBAL_KEY_NAME_SEPARATOR${key.name}"
        return cache.computeIfAbsent(name) {
            key.getAndroidKey(name)
        } as Preferences.Key<T>
    }
}