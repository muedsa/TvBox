package com.muedsa.util

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.core.content.pm.PackageInfoCompat

object AppUtil {

    fun getVersionInfo(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName}(${PackageInfoCompat.getLongVersionCode(packageInfo)})"
        } catch (_: Throwable) {
            ""
        }
    }

    fun debuggable(context: Context): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}