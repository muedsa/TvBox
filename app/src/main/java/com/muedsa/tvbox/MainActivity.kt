package com.muedsa.tvbox

import android.os.Bundle
import android.widget.Toast
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
import kotlinx.coroutines.withContext
import timber.log.Timber


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { PluginManager.isInit() }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Timber.i("init PluginManager")
                PluginManager.init(applicationContext)
            } catch (throwable: Throwable) {
                Timber.e(throwable, "init PluginManager error")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "init PluginManager error, ${throwable.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        setContent {
            TvTheme {
                Scaffold {
                    AppNavigation()
                }
            }
        }
    }
}