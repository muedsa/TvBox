package com.muedsa.tvbox.screens.detail

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.muedsa.tvbox.api.data.MediaHttpSource
import com.muedsa.tvbox.tool.ChromeUserAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

typealias MediaHttpSourceCallback = (MediaHttpSource?) -> Unit

class MediaUrlExtractor(
    private val context: Context,
    val mediaExtensions: List<String> = DEFAULT_MEDIA_EXTENSIONS,
    val timeout: Long = 30L,
) {
    private var webView: WebView? = null
    private var callback: MediaHttpSourceCallback? = null
    private val detectedUrls = mutableSetOf<String>() // 去重
    private var isExtracting = false

    // 开始提取视频地址
    fun extractVideoUrl(
        pageUrl: String,
        httpHeaders: Map<String, String>? = null,
        callback: MediaHttpSourceCallback,
    ) {
        if (isExtracting) return // 防止重复请求
        this.callback = callback
        isExtracting = true
        detectedUrls.clear()

        // 在子线程初始化 WebView（避免阻塞主线程）
        CoroutineScope(Dispatchers.Main).launch {
            initWebView()
            if (httpHeaders.isNullOrEmpty()) {
                webView?.loadUrl(pageUrl)
            } else {
                webView?.loadUrl(pageUrl, httpHeaders)
            }

            // 设置超时机制（10秒未获取到则返回null）
            launch(Dispatchers.IO) {
                delay(timeout * 1000L)
                if (isExtracting) {
                    Timber.d("webview 获取URL超时")
                    finishExtraction(null)
                }
            }
        }
    }

    // 初始化后台 WebView
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView = WebView(context.applicationContext).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = ChromeUserAgent
            webViewClient = object : WebViewClient() {
                // 拦截网络请求
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val url = request.url.toString()
                    Timber.d("webview request: $url")
                    if (isVideoUrl(request.url) && !detectedUrls.contains(url)) {
                        detectedUrls.add(url)
                        // 优先返回第一个有效视频地址
                        finishExtraction(
                            MediaHttpSource(
                                url = url,
                                httpHeaders = request.requestHeaders,
                            ),
                        )
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                // 页面加载失败时回调
//                override fun onReceivedError(
//                    view: WebView?,
//                    request: WebResourceRequest?,
//                    error: WebResourceError?
//                ) {
//                    super.onReceivedError(view, request, error)
//                    finishExtraction(null)
//                }
            }
        }
    }

    // 判断是否为视频地址
    private fun isVideoUrl(uri: Uri): Boolean {
        val lowercasePath = uri.path?.lowercase() ?: return false
        return mediaExtensions.any { lowercasePath.endsWith(it) }
    }

    // 结束提取并清理资源
    private fun finishExtraction(mediaHttpSource: MediaHttpSource?) {
        if (!isExtracting) return
        isExtracting = false
        callback?.invoke(mediaHttpSource)
        callback = null
        // 销毁 WebView 释放资源
        CoroutineScope(Dispatchers.Main).launch {
            webView?.stopLoading()
            webView?.webViewClient = WebViewClient() // 移除回调防止内存泄漏
            webView?.destroy()
            webView = null
        }
    }

    companion object {
        val DEFAULT_MEDIA_EXTENSIONS = listOf(".mp4", ".m3u8", ".avi", ".mov", ".flv", ".webm", ".mpd")
    }
}