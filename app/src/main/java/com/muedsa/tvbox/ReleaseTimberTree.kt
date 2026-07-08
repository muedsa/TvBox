package com.muedsa.tvbox

import timber.log.Timber

class ReleaseTimberTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
    }
}