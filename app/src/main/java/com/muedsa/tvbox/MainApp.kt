package com.muedsa.tvbox

import android.app.Application
import com.muedsa.util.AppUtil
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (AppUtil.debuggable(applicationContext)) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTimberTree())
        }
    }
}