package com.muedsa.tvbox

import android.content.Context
import androidx.room.Room
import com.muedsa.tvbox.danmaku.DanmakuService
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayApiService
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayAuthInterceptor
import com.muedsa.tvbox.danmaku.dandanplay.DanDanPlayDanmakuProvider
import com.muedsa.tvbox.room.AppDatabase
import com.muedsa.tvbox.store.DataStoreRepo
import com.muedsa.tvbox.tool.createJsonRetrofit
import com.muedsa.tvbox.tool.createOkHttpClient
import com.muedsa.util.AppUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideDanDanPlayDanmakuProvider(@ApplicationContext context: Context) =
        DanDanPlayDanmakuProvider(
            danDanPlayApiService = createJsonRetrofit(
                baseUrl = "https://api.dandanplay.net/api/",
                service = DanDanPlayApiService::class.java,
                okHttpClient = createOkHttpClient(debug = AppUtil.debuggable(context)) {
                    addInterceptor(DanDanPlayAuthInterceptor())
                }
            )
        )

    @Provides
    @Singleton
    fun provideDanmakuService(
        danDanPlayDanmakuProvider: DanDanPlayDanmakuProvider,
    ) = DanmakuService().also {
        if (BuildConfig.DANDANPLAY_APP_ID.isNotEmpty()
            && BuildConfig.DANDANPLAY_APP_SECRET.isNotEmpty()
        ) {
            it.register(danDanPlayDanmakuProvider)
        }
    }
}