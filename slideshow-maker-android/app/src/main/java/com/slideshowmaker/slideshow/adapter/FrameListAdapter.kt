package com.slideshowmaker.slideshow.adapter

import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.card.MaterialCardView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.FrameLinkInfo
import com.slideshowmaker.slideshow.data.models.isNone
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.utils.FileHelper
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.iconDownload
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.layoutThemeIcon
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.proBadge
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.themeIcon
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.themeName
import java.io.File

class FrameListAdapter :
    BaseAdapter<FrameLinkInfo>() {
    var onItemClickCallback: ((FrameLinkInfo) -> Unit)? = null
    private var mCurFrameFileName = "None"
    var isRewardLoaded = false

    override fun doGetViewType(position: Int): Int = R.layout.item_view_frame

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val cardView: MaterialCardView = holder.itemView as MaterialCardView
        val frameItem = _itemArray[position]
        cardView.themeName.text = frameItem.fileName

        if (frameItem.isNone()) {
            cardView.themeIcon.setImageResource(R.drawable.icon_none)
            cardView.iconDownload.visibility = View.GONE
        } else {
//             TODO: Pre download data - WIP
//            downloadFrame(
//                frameLinkData = item,
//                onComplete = {
//                }
//            )
            Glide.with(cardView.context)
                .load(frameItem.thumb)
                .into(cardView.themeIcon)

            if (File(FileHelper.frameFolderPath).resolve(frameItem.getRatioFileName()).exists()) {
                cardView.iconDownload.visibility = View.GONE
            } else {
                cardView.iconDownload.visibility = View.VISIBLE
            }
        }

        cardView.proBadge.isVisible = !SharedPreferUtils.proUser && frameItem.isPro


        if (mCurFrameFileName == frameItem.fileName) {
            cardView.layoutThemeIcon.setBackgroundResource(R.drawable.background_selected_transition)
        } else {
            cardView.layoutThemeIcon.setBackgroundResource(R.drawable.background_transparent)
        }

        cardView.setOnClickListener {
            onItemClickCallback?.invoke(frameItem)
        }
    }

    fun changeCurrentFrameName(frameLinkFile: String) {
        mCurFrameFileName = frameLinkFile
        notifyDataSetChanged()
    }

    private fun downloadFrame(
        frameLinkData: FrameLinkInfo,
        onComplete: () -> Unit,
    ) {
        val (link, fileName) = frameLinkData.getDownloadLinkAndName()
        PRDownloader.download(link, FileHelper.frameFolderPath, fileName)
            .setHeader("Accept-Encoding", "identity")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    if (fileName.endsWith(".zip")) {
                        FileHelper.unpackZip(FileHelper.frameFolderPath + "/", fileName)
                    }
                }

                override fun onError(error: Error?) {

                }
            })
    }
}