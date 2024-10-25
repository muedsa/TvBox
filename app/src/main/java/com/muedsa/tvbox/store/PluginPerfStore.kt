package com.muedsa.tvbox.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.muedsa.tvbox.api.store.IPluginPerfStore
import com.muedsa.tvbox.api.store.PluginPerfKey
import kotlinx.coroutines.flow.first

class PluginPerfStore(
    private val pluginPackage: String,
    private val pluginDataStore: DataStore<Preferences>
) : IPluginPerfStore {

    override suspend fun <T> get(key: PluginPerfKey<T>): T? {
        val globalKey = PluginKeyCache.getGlobalKey(pluginPackage = pluginPackage, key = key)
        return pluginDataStore.data.first().get(key = globalKey)
    }

    override suspend fun <T> getOrDefault(key: PluginPerfKey<T>, default: T): T {
        return get(key) ?: default
    }

    override suspend fun <T> update(key: PluginPerfKey<T>, value: T) {
        val globalKey = PluginKeyCache.getGlobalKey(pluginPackage = pluginPackage, key = key)
        pluginDataStore.edit {
            it[globalKey] = value
        }
    }


}