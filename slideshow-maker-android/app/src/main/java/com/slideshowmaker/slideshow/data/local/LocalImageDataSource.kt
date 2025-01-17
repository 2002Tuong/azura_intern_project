package com.slideshowmaker.slideshow.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.slideshowmaker.slideshow.data.response.SnapImage

class LocalImageDataSource(private val onFetchCallback: (limit: Int, offset: Int) -> List<SnapImage>) :
    PagingSource<Int, SnapImage>() {
    override fun getRefreshKey(state: PagingState<Int, SnapImage>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SnapImage> {
        val pageNumber = params.key ?: 0
        val pageSize = params.loadSize
        val pictures = onFetchCallback.invoke(pageSize, pageNumber * pageSize)
        val prevKey = if (pageNumber > 0) pageNumber - 1 else null
        val nextKey = if (pictures.isNotEmpty()) pageNumber + 1 else null
        return LoadResult.Page(
            data = pictures,
            prevKey = prevKey,
            nextKey = nextKey,
        )
    }
}
