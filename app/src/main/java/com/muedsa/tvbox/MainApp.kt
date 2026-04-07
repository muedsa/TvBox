package com.muedsa.tvbox

import android.app.Application
import android.os.Build
import com.muedsa.util.AppUtil
import dagger.hilt.android.HiltAndroidApp
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security

@HiltAndroidApp
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
        }
        if (AppUtil.debuggable(applicationContext)) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTimberTree())
        }
    }
}