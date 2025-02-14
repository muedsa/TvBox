package com.muedsa.tvbox.service

import com.muedsa.tvbox.BuildConfig
import com.muedsa.tvbox.tool.encodeBase64
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

class DanDanPlayAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val timestamp = System.currentTimeMillis() / 1000
        val path = chain.request().url.encodedPath
        val sign =
            "${BuildConfig.DANDANPLAY_APP_ID}$timestamp$path${BuildConfig.DANDANPLAY_APP_SECRET}".let {
                MessageDigest.getInstance("SHA-256")
                    .digest(it.toByteArray(Charsets.UTF_8))
                    .encodeBase64()
            }
        val request = chain.request().newBuilder()
            .header("X-AppId", BuildConfig.DANDANPLAY_APP_ID)
            .header("X-Timestamp", "$timestamp")
            .header("X-Signature", sign)
            .build()
        return chain.proceed(request)
    }
}