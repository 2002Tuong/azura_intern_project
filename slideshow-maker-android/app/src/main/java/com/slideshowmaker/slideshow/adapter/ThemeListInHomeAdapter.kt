package com.slideshowmaker.slideshow.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.card.MaterialCardView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.ThemeLinkInfo
import com.slideshowmaker.slideshow.data.models.zipfolder.ZipPageLoader
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.utils.FileHelper
import kotlinx.android.synthetic.main.item_view_theme_in_home.view.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class ThemeListInHomeAdapter(
    private val lifeScope: LifecycleCoroutineScope,
    private val context: Context
) : BaseAdapter<ThemeLinkInfo>() {

    var onItemClickCallback: ((ThemeLinkInfo) -> Unit)? = null
    private var mCurThemeFileName = "None"
    var isRewardLoaded = false
    override fun doGetViewType(position: Int): Int = R.layout.item_view_theme_in_home

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView: MaterialCardView = holder.itemView as MaterialCardView
        val themeItem = _itemArray[position]
        itemView.themeName.text = themeItem.name
        if (themeItem.link == "none") {
            itemView.themeIcon.setImageResource(R.drawable.icon_none)
            itemView.iconDownload.visibility = View.GONE
        } else {
            // TODO Pre download data - WIP
//            downloadTheme(
//                link = item.link,
//                fileName = item.fileName,
//                idTheme = item.id,
//                lottieFileLink = item.lottieLink,
//                lottieFileName = item.lottieFileName,
//            ) {
//
//            }
            Glide.with(itemView.context)
                .load(Uri.parse(themeItem.thumb))
                .into(itemView.themeIcon)

            if (File(FileHelper.themeFolderUnzip).resolve(themeItem.id).exists()) {
                itemView.iconDownload.visibility = View.GONE
            } else {
                itemView.iconDownload.visibility = View.VISIBLE
            }
        }

        itemView.proBadge.isVisible = !SharedPreferUtils.proUser && themeItem.isPro


        if (mCurThemeFileName == themeItem.fileName) {
            itemView.layoutThemeIcon.setBackgroundResource(R.drawable.background_selected_transition)

        } else {
            itemView.layoutThemeIcon.setBackgroundResource(R.drawable.background_transparent)
        }

        itemView.setOnClickListener {
            onItemClickCallback?.invoke(themeItem)
        }


    }

    fun changeCurrentThemeName(themeFileName: String) {
        mCurThemeFileName = themeFileName
        notifyDataSetChanged()
    }

    fun downloadTheme(
        link: String,
        fileName: String,
        idTheme: String,
        lottieFileLink: String,
        lottieFileName: String,
        onComplete: () -> Unit,
    )  {
        val lottieComplete = CompletableDeferred<Error?>()

        PRDownloader
            .download(lottieFileLink, FileHelper.themeFolderPath, lottieFileName)
            .build()
            .start(
                object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        lottieComplete.complete(null)
                        Timber.tag("onDownloadTheme")
                            .d("----> lottieFileLink=$lottieFileLink, lottieFileName=$lottieFileName --> DONE")
                    }

                    override fun onError(error: Error?) {
                        lottieComplete.complete(error)
                        Timber.tag("onDownloadTheme")
                            .d("----> lottieFileLink=$lottieFileLink, lottieFileName=$lottieFileName --> ERROR $error")
                    }
                }
            )

        PRDownloader.download(link, FileHelper.themeFolderPath, fileName)
            .setHeader("Accept-Encoding", "identity")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    lifeScope.launch {
                        val bug = lottieComplete.await()

                        if (bug == null) {
                            ZipPageLoader(
                                idTheme = idTheme,
                                File(FileHelper.themeFolderPath).resolve(fileName),
                                context
                            ).unzipEffect {
//                                Timber.tag("Download").d("onDownloadComplete")
//                                onComplete.invoke()
                            }
                        } else {
                            onError(bug)
                        }

                    }
                }

                override fun onError(error: Error?) {
                }
            })
    }

}