package com.parallax.hdvideo.wallpapers.ui.search

import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.fragment.MainFragmentAdapter

class SearchWallpaperAdapter(screenType: AppScreen, scrollListener : OnScrollListener) : MainFragmentAdapter(screenType, scrollListener = scrollListener) {

    override var screenType: AppScreen = AppScreen.SEARCH
    val videosDistance = 3
    private var typeOffilter = FiltersType.MIX
    private val listWallpaperModel = mutableListOf<WallpaperModel>()
    var couldLoadMoreVideo = true
    var couldLoadMorePhoto = true
    var listVideoStore = listOf<WallpaperModel>()

    override fun setData(data: List<WallpaperModel>) {
        listWallpaperModel.clear()
        listWallpaperModel.addAll(data)
        val data = filterData(data)
        super.setData(data)
    }

    fun addFilterData(data: List<WallpaperModel>): List<WallpaperModel> {
        listWallpaperModel.addAll(data)
        val res = filterData(data)
        super.addData(res)
        return res
    }

    override fun getItemId(position: Int): Long {
        return listData[position].hashCode().toLong()
    }

    fun setFilter(filter: FiltersType) {
        this.typeOffilter = filter
        super.setData(filterData(listWallpaperModel))
    }

    private fun filterData(list: List<WallpaperModel>) : List<WallpaperModel> {
        return when(typeOffilter) {
            FiltersType.MIX -> {
                super.canLoadMoreData(couldLoadMoreVideo || couldLoadMorePhoto)
                list
            }
            FiltersType.PHOTO -> {
                super.canLoadMoreData(couldLoadMorePhoto)
                list.filter { !it.isVideo }
            }
            FiltersType.VIDEO -> {
                super.canLoadMoreData(couldLoadMoreVideo)
                if (couldLoadMoreVideo) list.filter { it.isVideo } else (list.filter { it.isVideo } + listVideoStore)
            }
        }
    }

    override fun removeAll() {
        listVideoStore = listOf()
        super.removeAll()
    }

    override fun canLoadMoreData(isCan: Boolean) {
        couldLoadMoreVideo = isCan
        couldLoadMorePhoto = isCan
        super.canLoadMoreData(isCan)
    }

    fun mixSearchResults(images : List<WallpaperModel>, videosServer : List<WallpaperModel>) : List<WallpaperModel> {
        val videos = listVideoStore + videosServer
        listVideoStore = listOf()
        return when {
            videos.isEmpty() -> {
                images
            }
            images.isEmpty() -> {
                videos
            }
            else -> {
                val res = mutableListOf<WallpaperModel>()
                val size = videos.size
                val videosDistance = videosDistance
                var videoPosition = 0
                images.forEachIndexed { index, item ->
                    res.add(item)
                    val i = index / videosDistance
                    if (i < size && (index + 1) % videosDistance == 0) {
                        res.add(videos[i])
                        videoPosition = i
                    }
                }
                val start = videoPosition + 1
                if (start < size) {
                    if (!couldLoadMoreVideo && !couldLoadMorePhoto) {
                        for (i in start until size) {
                            res.add(videos[i])
                        }
                    } else {
                        listVideoStore = videos.subList(start, size)
                    }
                }
                res
            }
        }
    }

    enum class FiltersType {
        MIX, PHOTO, VIDEO
    }
}