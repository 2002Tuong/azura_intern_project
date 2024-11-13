package com.artgen.app.ui.screen.imagepicker

import androidx.paging.PagingSource
import androidx.paging.PagingState
import snapedit.app.remove.data.Image

class ImageDataSource(private val onFetch: (limit: Int, offset: Int) -> List<Image>) :
    PagingSource<Int, Image>() {
    override fun getRefreshKey(state: PagingState<Int, Image>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> {
        val pageNumber = params.key ?: 0
        val pageSize = params.loadSize
        val pictures = onFetch.invoke(pageSize, pageNumber * pageSize)
        val prevKey = if (pageNumber > 0) pageNumber - 1 else null
        val nextKey = if (pictures.isNotEmpty()) pageNumber + 1 else null
        return LoadResult.Page(
            data = pictures,
            prevKey = prevKey,
            nextKey = nextKey,
        )
    }
}
