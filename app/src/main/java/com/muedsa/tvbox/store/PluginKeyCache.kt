package com.muedsa.tvbox.store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.muedsa.tvbox.api.store.PluginPerfKey

object PluginKeyCache {
    private val cache: MutableMap<String, Preferences.Key<*>> = mutableMapOf()

    private const val GLOBAL_KEY_NAME_SEPARATOR = ":"

    fun getGlobalKeyPrefix(pluginPackage: String): String = "$pluginPackage$GLOBAL_KEY_NAME_SEPARATOR"

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T> getGlobalKey(pluginPackage: String, key: PluginPerfKey<T>): Preferences.Key<T> {
        val name = "${getGlobalKeyPrefix(pluginPackage)}${key.name}"
        return cache.computeIfAbsent(name) {
            getAndroidKey(key)
        } as Preferences.Key<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getAndroidKey(key: PluginPerfKey<T>): Preferences.Key<T> {
        return when(key) {
            is PluginPerfKey.IntPluginPerfKey -> intPreferencesKey(key.name)
            is PluginPerfKey.DoublePluginPerfKey -> doublePreferencesKey(key.name)
            is PluginPerfKey.StringPluginPerfKey -> stringPreferencesKey(key.name)
            is PluginPerfKey.BooleanPluginPerfKey -> booleanPreferencesKey(key.name)
            is PluginPerfKey.FloatPluginPerfKey -> floatPreferencesKey(key.name)
            is PluginPerfKey.LongPluginPerfKey -> longPreferencesKey(key.name)
            is PluginPerfKey.StringSetPluginPerfKey -> stringSetPreferencesKey(key.name)
            is PluginPerfKey.ByteArrayPluginPerfKey -> byteArrayPreferencesKey(key.name)
        } as Preferences.Key<T>
    }
}