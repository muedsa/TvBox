package com.muedsa.tvbox

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber

class ReleaseTimberTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        Firebase.crashlytics.log("[${tag ?: "app"}]$message")
        if (t != null) {
            Firebase.crashlytics.recordException(t)
        }
    }
}