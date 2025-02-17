package com.muedsa.util

import okhttp3.Interceptor
import okhttp3.Response

class OkHttpCacheInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var rep = chain.request()
        val focusedCacheControl = rep.header(HEADER)
        if (!focusedCacheControl.isNullOrEmpty()) {
            rep = rep.newBuilder().removeHeader(HEADER).build()
        }
        var resp = chain.proceed(rep)

        if (!focusedCacheControl.isNullOrEmpty()) {
            resp = resp.newBuilder()
                .header("Cache-Control", focusedCacheControl)
                .header("Pragma", "")
                .build()
        }
        return resp
    }

    companion object {
        const val HEADER = "Focused-Cache-Control"
    }
}