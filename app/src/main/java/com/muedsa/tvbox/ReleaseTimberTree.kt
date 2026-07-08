package com.muedsa.tvbox

import io.sentry.Sentry
import timber.log.Timber

class ReleaseTimberTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        if (t != null) {
            Sentry.captureException(t)
        }
    }
}