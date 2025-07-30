package com.muedsa.tvbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.lifecycleScope
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.muedsa.compose.tv.theme.TvTheme
import com.muedsa.compose.tv.widget.FillTextScreen
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.tool.IPv6Checker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

// @AndroidEntryPoint 暂时无需依赖注入
class InstallPluginActivity : ComponentActivity() {

    var uiState by mutableStateOf("正在安装插件... {{{(>_<)}}}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        if (uri == null) {
            finish()
            return
        }
        lifecycleScope.launch {
            runCatching {
                uiState = "💡正在安装插件... {{{(>_<)}}}\n${uri}"
                val iPv6Status = withContext(Dispatchers.IO) {
                    IPv6Checker.checkIPv6Support()
                }
                PluginManager.init(context = applicationContext, iPv6Status = iPv6Status)
                PluginManager.installPlugin(context = applicationContext, uri = uri)
            }.onSuccess {
                uiState = "✅插件安装成功!!! ╰(*°▽°*)╯\n${uri}"
            }.onFailure {
                uiState = "❌插件安装失败!!! (╯°□°）╯︵ ┻━┻\n${it.javaClass.name}: ${it.message}\n${uri}"
                Timber.e(it, "install plugin error: $uri")
            }
        }
        setContent {
            TvTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    shape = RectangleShape,
                    colors = SurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    FillTextScreen(uiState)
                }
            }
        }
    }
}