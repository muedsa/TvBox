package com.muedsa.tvbox.plugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.muedsa.tvbox.api.plugin.IPlugin
import com.muedsa.tvbox.api.plugin.TvBoxContext
import com.muedsa.tvbox.store.PluginPerfStore
import com.muedsa.tvbox.tool.IPv6Checker
import com.muedsa.util.AppUtil
import dalvik.system.PathClassLoader
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File


object PluginManager {

    const val PLUGIN_FILE_SUFFIX = ".tbp"
    private lateinit var pluginDir: File
    private lateinit var pluginOATDir: File
    private lateinit var sharedTvBoxContext: SharedTvBoxContext

    private val mutex = Mutex()
    private var _pluginInfoMap: Map<String, PluginInfo>? = null
    private val _pluginPool: MutableMap<String, Plugin> = mutableMapOf()
    private var _currentPlugin: Plugin? = null
    fun getCurrentPlugin(): Plugin =
        _currentPlugin ?: throw RuntimeException("插件还未初始化")


    @Synchronized
    fun init(context: Context) {
        if (!isInit()) {
            pluginDir = context.getExternalFilesDir("plugins")!!
//        pluginDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            .resolve("com.muedsa.tvbox")
//            .resolve("plugins")
//            .apply { mkdirs() }
            pluginOATDir = pluginDir.resolve("oat")
            sharedTvBoxContext = SharedTvBoxContext(
                screenWidth = context.resources.configuration.screenWidthDp,
                screenHeight = context.resources.configuration.screenHeightDp,
                debug = AppUtil.debuggable(context),
                iPv6Status = IPv6Checker.checkIPv6Support()
            )
        }
    }

    fun isInit(): Boolean = PluginManager::pluginDir.isInitialized
            && PluginManager::pluginOATDir.isInitialized
            && PluginManager::sharedTvBoxContext.isInitialized

    fun getPluginDir() = pluginDir

    @SuppressLint("QueryPermissionsNeeded")
    suspend fun loadPlugins(context: Context): LoadedPlugins = mutex.withLock {
        val packageManager = context.packageManager
        _pluginInfoMap = null
        _currentPlugin = null
        _pluginPool.clear()

        val pluginMap = mutableMapOf<String, PluginInfo>()
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
                    Timber.e(it, "加载插件失败 ${pluginFile.absolutePath}")
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
                        Timber.e(it, "加载外部插件失败 ${info.packageName}")
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
                val pluginInstance = pluginClz.getDeclaredConstructor(TvBoxContext::class.java)
                    .newInstance(
                        sharedTvBoxContext.createTvBoxContext(
                            store = PluginPerfStore(
                                pluginPackage = pluginInfo.packageName,
                                pluginDataStore = pluginDataStore
                            )
                        )
                    ) as IPlugin
                runBlocking {
                    pluginInstance.onInit()
                }
                Plugin(
                    pluginInfo = pluginInfo,
                    pluginInstance = pluginInstance
                )
            } catch (e: Exception) {
                Timber.e(e)
                throw RuntimeException("初始化插件失败")
            }
        }.let {
            _currentPlugin = it
            it.pluginInstance.onLaunched()
        }
    }

    suspend fun installPlugin(context: Context, file: File) = mutex.withLock {
        val packageManager = context.packageManager
        val info = parsePluginInfo(packageManager, file.absolutePath)
        val newFile = pluginDir.resolve(info.packageName + PLUGIN_FILE_SUFFIX)
        file.copyTo(newFile, true).setReadOnly()
    }

    suspend fun uninstallPlugin(pluginInfo: PluginInfo): Boolean =
        mutex.withLock {
            val pluginFile = File(pluginInfo.sourcePath)
            var flag = false
            if (pluginFile.exists()) {
                flag = pluginFile.deleteRecursively()
                if (flag) Timber.d("delete file ${pluginFile.absolutePath}")
                if (pluginOATDir.exists() && pluginOATDir.isDirectory) {
                    pluginOATDir.listFiles()
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