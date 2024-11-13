package com.slideshowmaker.slideshow.adapter

import android.view.View
import com.bumptech.glide.Glide
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.FilterLinkInfo
import com.slideshowmaker.slideshow.data.models.isNone
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.ui.slide_show_v2.localFile
import com.slideshowmaker.slideshow.utils.FileHelper
import kotlinx.android.synthetic.main.item_view_lookup.view.*
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.iconDownload
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.layoutThemeIcon
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.themeIcon
import timber.log.Timber
import kotlin.math.roundToInt

class ImageFilterListAdapter(private val onSelectLookup: (FilterLinkInfo) -> Unit) :
    BaseAdapter<FilterLinkInfo>() {
    private var mCurFilterId: String? = null

    override fun doGetViewType(position: Int): Int = R.layout.item_view_lookup

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val filterItem = _itemArray[position]
        itemView.setOnClickListener {
            onSelectLookup.invoke(filterItem)
        }
        if (mCurFilterId == filterItem.id) {
            itemView.layoutThemeIcon.setBackgroundResource(R.drawable.background_selected_transition)
        } else {
            itemView.layoutThemeIcon.setBackgroundResource(R.drawable.background_transparent)
        }
        if (filterItem.isNone()) {
            itemView.themeIcon.setImageResource(R.drawable.icon_none)
            itemView.iconDownload.visibility = View.GONE
        } else {
            // TODO: Pre download data - WIP
//            downloadFilter(item) {
//
//            }
            Glide.with(itemView.context)
                .load(filterItem.thumb)
                .into(itemView.themeIcon)

            if (filterItem.localFile().exists()) {
                itemView.iconDownload.visibility = View.GONE
            } else {
                itemView.iconDownload.visibility = View.VISIBLE
            }
        }
        itemView.filterName.text = filterItem.name
    }

    fun changeCurrentFilter(id: String) {
        mCurFilterId = id
        notifyDataSetChanged()
    }

    fun downloadFilter(
        filterLinkInfo: FilterLinkInfo,
        onComplete: () -> Unit,
    ) {
        val (link, fileName) = filterLinkInfo.getDownloadLinkAndName()
        PRDownloader.download(link, FileHelper.filterFolderPath, fileName)
            .setHeader("Accept-Encoding", "identity")
            .build()
            .setOnProgressListener {
                val progress = it.currentBytes.toFloat() / it.totalBytes
                Timber.tag("Download").d("===> $link ==> ${(progress * 100f).roundToInt()}")
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    if (fileName.endsWith(".zip")) {
                        FileHelper.unpackZip(FileHelper.filterFolderPath + "/", fileName)
                    }
                }

                override fun onError(error: Error?) {

                }
            })
    }
}