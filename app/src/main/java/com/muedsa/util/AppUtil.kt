package com.muedsa.util

import android.content.Context
import android.content.pm.ApplicationInfo

object AppUtil {

    fun getVersionInfo(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
                "${packageInfo.versionName}(${packageInfo.longVersionCode})"
            else
                "${packageInfo.versionName}(${packageInfo.versionCode})"
        } catch (t: Throwable) {
            ""
        }
    }

    fun debuggable(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}