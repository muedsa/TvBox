package com.muedsa.tvbox

import android.content.Context
import androidx.room.Room
import com.muedsa.tvbox.danmaku.DanmakuService
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayApiService
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayAuthInterceptor
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayDanmakuProvider
import com.muedsa.tvbox.danmaku.iqiyi.IqiyiDanmakuProvider
import com.muedsa.tvbox.danmaku.iqiyi.IqiyiSearchApiService
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
    fun provideOkhttpCookieJar(dataStoreRepo: DataStoreRepo) = PluginCookieJar(
        saver = SharedCookieSaver(
            store = PluginPerfStore(
                pluginPackage = BuildConfig.APPLICATION_ID,
                pluginDataStore = dataStoreRepo.dataStore,
            ),
        ),
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
    fun provideDanmakuService(
        danDanPlayDanmakuProvider: DanDanPlayDanmakuProvider,
        iqiyiDanmakuProvider: IqiyiDanmakuProvider,
    ) = DanmakuService().also {
        if (BuildConfig.DANDANPLAY_APP_ID.isNotEmpty()
            && BuildConfig.DANDANPLAY_APP_SECRET.isNotEmpty()
        ) {
            it.register(danDanPlayDanmakuProvider)
        }
        it.register(iqiyiDanmakuProvider)
    }
}