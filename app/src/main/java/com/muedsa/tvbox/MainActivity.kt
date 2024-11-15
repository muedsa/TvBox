package com.muedsa.tvbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.muedsa.compose.tv.theme.TvTheme
import com.muedsa.compose.tv.widget.Scaffold
import com.muedsa.tvbox.plugin.PluginManager
import com.muedsa.tvbox.screens.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { PluginManager.isInit() }
        lifecycleScope.launch(Dispatchers.IO) {
            PluginManager.init(applicationContext)
        }
        super.onCreate(savedInstanceState)
        setContent {
            TvTheme {
                Scaffold {
                    AppNavigation()
                }
            }
        }
    }
}