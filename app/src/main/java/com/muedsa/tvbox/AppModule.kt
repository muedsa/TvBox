package com.muedsa.tvbox

import android.content.Context
import androidx.room.Room
import com.muedsa.tvbox.danmaku.DanmakuService
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayApiService
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayAuthInterceptor
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayDanmakuProvider
import com.muedsa.tvbox.danmaku.iqiyi.IqiyiDanmakuProvider
import com.muedsa.tvbox.danmaku.iqiyi.IqiyiSearchApiService
import com.muedsa.tvbox.danmaku.tencent.TencentDanmakuProvider
import com.muedsa.tvbox.danmaku.tencent.TencentVideoApiService
import com.muedsa.tvbox.danmaku.youku.YoukuApiService
import com.muedsa.tvbox.danmaku.youku.YoukuDanmakuProvider
import com.muedsa.tvbox.room.AppDatabase
import com.muedsa.tvbox.store.DataStoreRepo
import com.muedsa.tvbox.store.PluginPerfStore
import com.muedsa.tvbox.tool.PluginCookieJar
import com.muedsa.tvbox.tool.SharedCookieSaver
import com.muedsa.tvbox.tool.createJsonRetrofit
import com.muedsa.tvbox.tool.createOkHttpClient
import com.muedsa.util.AppUtil
import com.muedsa.util.OkHttpCacheInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "TvBox"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteMediaDao(appDatabase: AppDatabase) = appDatabase.favoriteMediaDao()

    @Provides
    @Singleton
    fun provideEpisodeProgressDao(appDatabase: AppDatabase) = appDatabase.episodeProgressDao()

    @Singleton
    @Provides
    fun provideDataStoreRepository(@ApplicationContext app: Context) = DataStoreRepo(app)

    @Provides
    @Singleton
    fun provideOkhttpCache(@ApplicationContext context: Context) = Cache(
        directory = context.cacheDir.resolve("http_cache"),
        maxSize = 50 * 1024 * 1024,
    )

    @Provides
    @Singleton
    fun provideSharedCookieSaver(dataStoreRepo: DataStoreRepo) = SharedCookieSaver(
        store = PluginPerfStore(
            pluginPackage = BuildConfig.APPLICATION_ID,
            pluginDataStore = dataStoreRepo.dataStore,
        ),
    )

    @Provides
    @Singleton
    fun provideOkhttpCookieJar(cookieSaver: SharedCookieSaver) = PluginCookieJar(
        saver = cookieSaver,
    )

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        okHttpCache: Cache,
        cookieJar: PluginCookieJar,
    ) = createOkHttpClient(debug = AppUtil.debuggable(context)) {
        cache(okHttpCache)
        addNetworkInterceptor(OkHttpCacheInterceptor())
        cookieJar(cookieJar)
    }

    @Provides
    @Singleton
    fun provideDanDanPlayDanmakuProvider(
        @ApplicationContext context: Context,
        okHttpCache: Cache,
    ) = DanDanPlayDanmakuProvider(
            danDanPlayApiService = createJsonRetrofit(
                baseUrl = "https://api.dandanplay.net/api/",
                service = DanDanPlayApiService::class.java,
                okHttpClient = createOkHttpClient(debug = AppUtil.debuggable(context)) {
                    cache(okHttpCache)
                    addInterceptor(DanDanPlayAuthInterceptor())
                    addNetworkInterceptor(OkHttpCacheInterceptor())
                }
            )
        )

    @Provides
    @Singleton
    fun provideIqiyiDanmakuProvider(okHttpClient: OkHttpClient) = IqiyiDanmakuProvider(
        okHttpClient = okHttpClient,
        iqiyiSearchApiService = createJsonRetrofit(
            baseUrl = "https://search.video.iqiyi.com/",
            service = IqiyiSearchApiService::class.java,
            okHttpClient = okHttpClient,
        ),
    )

    @Provides
    @Singleton
    fun provideYoukuDanmakuProvider(
        cookieSaver: SharedCookieSaver,
        okHttpClient: OkHttpClient,
    ) = YoukuDanmakuProvider(
        cookieSaver = cookieSaver,
        okHttpClient = okHttpClient,
        youkuApiService = createJsonRetrofit(
            baseUrl = "https://openapi.youku.com/",
            service = YoukuApiService::class.java,
            okHttpClient = okHttpClient,
        ),
    )

    @Provides
    @Singleton
    fun provideTencentDanmakuProvider(
        okHttpClient: OkHttpClient,
    ) = TencentDanmakuProvider(
        tencentVideoApiService = createJsonRetrofit(
            baseUrl = "https://pbaccess.video.qq.com/",
            service = TencentVideoApiService::class.java,
            okHttpClient = okHttpClient,
        ),
    )

    @Provides
    @Singleton
    fun provideDanmakuService(
        danDanPlayDanmakuProvider: DanDanPlayDanmakuProvider,
        iqiyiDanmakuProvider: IqiyiDanmakuProvider,
        youkuDanmakuProvider: YoukuDanmakuProvider,
        tencentDanmakuProvider: TencentDanmakuProvider,
    ) = DanmakuService().also {
        if (BuildConfig.DANDANPLAY_APP_ID.isNotEmpty()
            && BuildConfig.DANDANPLAY_APP_SECRET.isNotEmpty()
        ) {
            it.register(danDanPlayDanmakuProvider)
        }
        it.register(iqiyiDanmakuProvider)
        it.register(youkuDanmakuProvider)
        it.register(tencentDanmakuProvider)
    }
}