package com.muedsa.tvbox.screens.plugin.catalog

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.muedsa.tvbox.api.data.MediaCard
import com.muedsa.tvbox.api.data.MediaCatalogConfig
import com.muedsa.tvbox.api.data.MediaCatalogOption
import com.muedsa.tvbox.api.service.IMediaCatalogService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaCatalogPagingSource(
    private val config: MediaCatalogConfig,
    private val service: IMediaCatalogService,
    private val options: List<MediaCatalogOption> = emptyList()
) : PagingSource<String, MediaCard>() {

    private val keyCache = mutableMapOf<String,Pair<String?, String?>>()

    override suspend fun load(params: LoadParams<String>): LoadResult<String, MediaCard> {
        val loadKey = params.key ?: config.initKey
        return try {
            val result = withContext(Dispatchers.IO) {
                service.catalog(options = options, loadKey = loadKey, params.loadSize)
            }
            keyCache.put(loadKey, result.prevKey to result.nextKey)
            LoadResult.Page(
                data = result.list,
                prevKey = result.prevKey,
                nextKey = result.nextKey
            )
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            return LoadResult.Error(throwable)
        }
    }

    override fun getRefreshKey(state: PagingState<String, MediaCard>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            keyCache[page?.nextKey]?.first ?: keyCache[page?.prevKey]?.second
        }
    }
}