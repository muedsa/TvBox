package com.muedsa.tvbox.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject

private const val PREFS_NAME = "setting"
private const val PLUGIN_PREFS_NAME = "plugin_setting"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)
private val Context.pluginDataStore: DataStore<Preferences> by preferencesDataStore(name = PLUGIN_PREFS_NAME)

class DataStoreRepo @Inject constructor(context: Context) {
    val dataStore: DataStore<Preferences> = context.dataStore
    val pluginDataStore: DataStore<Preferences> = context.pluginDataStore
}