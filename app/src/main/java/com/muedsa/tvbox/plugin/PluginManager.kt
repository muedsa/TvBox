package com.muedsa.tvbox.plugin

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.muedsa.tvbox.api.plugin.IPlugin
import com.muedsa.tvbox.api.plugin.TvBoxContext
import com.muedsa.tvbox.store.PluginPerfStore
import com.muedsa.util.AppUtil
import dalvik.system.PathClassLoader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File


object PluginManager {

    private val mutex = Mutex()

    private var _pluginInfoMap: Map<String, PluginInfo>? = null
    fun getPluginInfoList(): List<PluginInfo>? = _pluginInfoMap?.values?.toList()

    private val _pluginPool: MutableMap<String, Plugin> = mutableMapOf()
    private var _currentPlugin: Plugin? = null
    fun getCurrentPlugin(): Plugin =
        _currentPlugin ?: throw RuntimeException("插件还未初始化")

    const val PLUGIN_FILE_SUFFIX = ".tbp"

    fun getPluginDir(context: Context): File =
        context.getExternalFilesDir("plugins")!!
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            .resolve("com.muedsa.tvbox")
//            .resolve("plugins")
//            .apply { mkdirs() }

    fun getPluginOATDir(context: Context): File = getPluginDir(context).resolve("oat")

    private var _tvBoxContext: TvBoxContext? = null

    @Synchronized
    private fun getTvBoxContext(
        context: Context,
        pluginInfo: PluginInfo,
        pluginDataStore: DataStore<Preferences>
    ): TvBoxContext {
        return _tvBoxContext ?: TvBoxContext(
            screenWidth = context.resources.configuration.screenWidthDp,
            screenHeight = context.resources.configuration.screenHeightDp,
            debug = AppUtil.debuggable(context),
            store = PluginPerfStore(pluginPackage = pluginInfo.packageName, pluginDataStore = pluginDataStore)
        )
    }

    suspend fun loadPlugins(context: Context): LoadedPlugins = mutex.withLock {
        val packageManager = context.packageManager
        _pluginInfoMap = null
        _currentPlugin = null
        _pluginPool.clear()

        val pluginMap = mutableMapOf<String, PluginInfo>()
        val pluginDir = getPluginDir(context)
        Timber.d("插件目录: ${pluginDir.absolutePath}")
        val invalidFiles = mutableListOf<File>()
        pluginDir.listFiles()
            ?.filter {
                if (!it.isFile) {
                    Timber.d("扫描到非文件 ${it.name}")
                    invalidFiles.add(it)
                    return@filter false
                }
                if (!it.absolutePath.endsWith(PLUGIN_FILE_SUFFIX)) {
                    Timber.d("扫描到非插件文件 ${it.name}")
                    invalidFiles.add(it)
                    return@filter false
                }
                if (!it.canRead()) {
                    Timber.d("扫描到无文件读取权限插件 ${it.name}")
                    invalidFiles.add(it)
                    return@filter false
                }
                return@filter true
            }?.forEach { pluginFile ->
                runCatching {
                    Timber.d("尝试加载插件 ${pluginFile.name}")
                    parsePluginInfo(packageManager, pluginFile.absolutePath)
                        .also {
                            pluginMap[it.packageName] = it
                        }
                }.onFailure {
                    Timber.e("加载插件失败 ${pluginFile.absolutePath}", it)
                }
            }

        if (AppUtil.debuggable(context)) {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.packageName != "com.muedsa.tvbox" && it.packageName != "com.muedsa.debug" }
                .filter { it.metaData?.getString("tv_box_plugin_key") == "com.muedsa.tvbox" }
                .forEach { info ->
                    val path = info.sourceDir
                    Timber.d("尝试加载外部插件 $path")
                    kotlin.runCatching {
                        parsePluginInfo(packageManager, path).also {
                            it.isExternalPlugin = true
                            pluginMap[it.packageName] = it
                        }
                    }.onFailure {
                        Timber.e("加载外部插件失败 ${info.packageName}", it)
                    }
                }
        }

        _pluginInfoMap = pluginMap

        return@withLock LoadedPlugins(
            plugins = pluginMap.values.toList(),
            invalidFiles = invalidFiles
        )
    }

    private fun parsePluginInfo(
        packageManager: PackageManager,
        pluginPackagePath: String
    ): PluginInfo {
        val packageArchiveInfo =
            packageManager.getPackageArchiveInfo(pluginPackagePath, PackageManager.GET_META_DATA)
                ?: throw RuntimeException("插件文件不存在 $pluginPackagePath")
        packageArchiveInfo.applicationInfo?.apply {
            if (publicSourceDir.isNullOrEmpty())
                publicSourceDir = pluginPackagePath
            if (sourceDir.isNullOrEmpty())
                sourceDir = pluginPackagePath
        }
        return parsePluginInfo(packageManager, packageArchiveInfo)
    }

    private fun parsePluginInfo(
        packageManager: PackageManager,
        pluginPackageInfo: PackageInfo
    ): PluginInfo {
        val pluginApplicationInfo = pluginPackageInfo.applicationInfo!!
        val apiVersion = pluginApplicationInfo.metaData.getInt("tv_box_plugin_api_version", -1)
        if (apiVersion < 1) {
            throw RuntimeException("tv_box_plugin_api_version not found")
        }
        val entryPointImpl =
            pluginApplicationInfo.metaData.getString("tv_box_plugin_entry_point_impl")
                ?: throw RuntimeException("tv_box_plugin_entry_point_impl not found")
        return PluginInfo(
            apiVersion = apiVersion,
            entryPointImpl = entryPointImpl,
            packageName = pluginApplicationInfo.packageName,
            name = pluginApplicationInfo.loadLabel(packageManager).toString(),
            versionName = pluginPackageInfo.versionName ?: "",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                pluginPackageInfo.longVersionCode else pluginPackageInfo.versionCode.toLong(),
            icon = pluginApplicationInfo.loadIcon(packageManager),
            sourcePath = pluginApplicationInfo.sourceDir
        )
    }

    suspend fun launchPlugin(
        context: Context,
        pluginInfo: PluginInfo,
        pluginDataStore: DataStore<Preferences>
    ) = mutex.withLock {
        _pluginPool.computeIfAbsent(pluginInfo.sourcePath) {
            val pluginFile = File(pluginInfo.sourcePath)
            if (!pluginFile.exists() || !pluginFile.isFile) throw RuntimeException("plugin file not found")
            val classLoader = PathClassLoader(pluginFile.path, context.classLoader)
            try {
                val pluginClz = classLoader.loadClass(pluginInfo.entryPointImpl)
                Plugin(
                    pluginInfo = pluginInfo,
                    pluginInstance = (pluginClz.getDeclaredConstructor(TvBoxContext::class.java)
                        .newInstance(getTvBoxContext(
                            context = context,
                            pluginInfo = pluginInfo,
                            pluginDataStore = pluginDataStore
                        )) as IPlugin).apply { onInit() }
                )
            } catch (e: Exception) {
                Timber.e(e)
                throw RuntimeException("初始化插件失败")
            }
        }.let {
            _currentPlugin = it
        }
    }

    suspend fun installPlugin(context: Context, file: File) = mutex.withLock {
        val packageManager = context.packageManager
        val info = parsePluginInfo(packageManager, file.absolutePath)
        val newFile = getPluginDir(context).resolve(info.packageName + PLUGIN_FILE_SUFFIX)
        file.copyTo(newFile, true).setReadOnly()
    }

    suspend fun uninstallPlugin(context: Context, pluginInfo: PluginInfo): Boolean =
        mutex.withLock {
            val pluginFile = File(pluginInfo.sourcePath)
            var flag = false
            if (pluginFile.exists()) {
                flag = pluginFile.deleteRecursively()
                if (flag) Timber.d("delete file ${pluginFile.absolutePath}")
                val oatDir = getPluginOATDir(context)
                if (oatDir.exists() && oatDir.isDirectory) {
                    oatDir.listFiles()
                        ?.filter { it.isDirectory }
                        ?.forEach { childDir ->
                            childDir.listFiles()
                                ?.filter {
                                    it.isFile && (it.name == "${pluginInfo.packageName}.odex"
                                            || it.name == "${pluginInfo.packageName}.vdex")
                                }
                                ?.forEach { if (it.deleteRecursively()) Timber.d("delete file ${it.absolutePath}") }
                        }
                }
            }
            return@withLock flag
        }
}