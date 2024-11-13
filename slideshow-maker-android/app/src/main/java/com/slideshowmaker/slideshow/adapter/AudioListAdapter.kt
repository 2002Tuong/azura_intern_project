package com.slideshowmaker.slideshow.adapter

import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.core.view.isVisible
import coil.load
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.data.models.MusicReturnInfo
import com.slideshowmaker.slideshow.models.AudioModel
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import com.slideshowmaker.slideshow.ui.custom.ControlSliderStartEnd
import kotlinx.android.synthetic.main.item_view_music_list.view.audioControllerEdit
import kotlinx.android.synthetic.main.item_view_music_list.view.btnTrim
import kotlinx.android.synthetic.main.item_view_music_list.view.buttonUseMusic
import kotlinx.android.synthetic.main.item_view_music_list.view.icPlayAndPause
import kotlinx.android.synthetic.main.item_view_music_list.view.iconMusic
import kotlinx.android.synthetic.main.item_view_music_list.view.layoutDownload
import kotlinx.android.synthetic.main.item_view_music_list.view.musicDurationLabel
import kotlinx.android.synthetic.main.item_view_music_list.view.musicNameLabel

class AudioListAdapter(val callback: MusicCallback) : BaseAdapter<AudioModel>() {

    private var choosenGender: String = ""

    var curPlayingMusic: String = ""
    override fun doGetViewType(position: Int): Int = R.layout.item_view_music_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView = holder.itemView
        val audioItem = _itemArray[position]

        itemView.musicNameLabel.text = audioItem.name
        itemView.musicDurationLabel.text = audioItem.durationInString

        if (audioItem.isSelected) {
            itemView.buttonUseMusic.visibility = View.VISIBLE
            itemView.audioControllerEdit.apply {
                setMaxValue(audioItem.duration)
            }

        } else {
            itemView.buttonUseMusic.visibility = View.GONE

        }
        itemView.iconMusic.load(audioItem.thumbnailUrl) {
            placeholder(R.drawable.icon_play_circle)
            error(R.drawable.icon_play_circle)
        }

        if (audioItem.filePath == curPlayingMusic) itemView.icPlayAndPause.setImageResource(R.drawable.icon_pause_24)
        if (audioItem.filePath == curPlayingMusic) {
            val rotation = RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotation.duration = 2000
            rotation.repeatCount = Animation.INFINITE
            rotation.repeatMode = Animation.RESTART
            itemView.iconMusic.startAnimation(rotation)
        } else {
            itemView.icPlayAndPause.setImageResource(R.drawable.icon_play)
            itemView.iconMusic.rotation = 0f
            itemView.iconMusic.clearAnimation()
        }
        itemView.layoutDownload.isVisible = audioItem.filePath.startsWith("https")
        itemView.buttonUseMusic.isVisible = !audioItem.filePath.startsWith("https")
        itemView.layoutDownload.setOnClickListener {
            callback.onClickDownload(audioItem)
        }
        itemView.btnTrim.setOnClickListener {
            itemView.performClick()
        }
        itemView.setOnClickListener {
            if (audioItem.filePath.startsWith("https")) return@setOnClickListener
            if (_curItem == audioItem) return@setOnClickListener

            if (_curItem?.duration == 0L) {

                return@setOnClickListener
            }

            _curItem?.let {
                it.isSelected = false
                it.isPlaying = false
                it.reset()
            }
            _curItem = audioItem
            _curItem?.let {
                it.isSelected = true
                it.isPlaying = true
            }
            curPlayingMusic = _curItem?.filePath.orEmpty()
            callback.onClickItem(audioItem)
            notifyDataSetChanged()
        }

        itemView.audioControllerEdit.setStartAndEndProgress(
            audioItem.startOffset * 100f / audioItem.duration,
            (audioItem.startOffset + audioItem.length) * 100f / audioItem.duration
        )

        itemView.audioControllerEdit.setOnChangeListener(object :
            ControlSliderStartEnd.OnChangeListener {
            override fun onSwipeLeft(progress: Float) {

            }

            override fun onLeftUp(progress: Float) {
                audioItem.startOffset = itemView.audioControllerEdit.getStartOffset()
                audioItem.length = itemView.audioControllerEdit.getLength().toLong()
                callback.onChangeStart(audioItem.startOffset, audioItem.length.toInt())
            }

            override fun onSwipeRight(progress: Float) {

            }

            override fun onRightUp(progress: Float) {
                audioItem.length = itemView.audioControllerEdit.getLength().toLong()
                callback?.onChangeEnd(audioItem.length.toInt())
            }

        })

        itemView.buttonUseMusic.setOnClickListener {
            callback.onClickUse(audioItem)
        }

        itemView.iconMusic.setOnClickListener {
            if (audioItem.filePath.startsWith("https")) return@setOnClickListener
            audioItem.isPlaying = !audioItem.isPlaying
            callback.onClickPlay(audioItem.isPlaying, audioItem)
            if (audioItem.isPlaying) {
                curPlayingMusic = audioItem.filePath
            } else {
                curPlayingMusic = ""
            }
            notifyDataSetChanged()
        }
    }

    fun setAudioDataList(audioInfoList: ArrayList<AudioInfo>) {
        _itemArray.clear()
        notifyDataSetChanged()
        for (audio in audioInfoList) {
            _itemArray.add(AudioModel(audio))
        }
        notifyDataSetChanged()
    }

    fun restoreBeforeMusic(musicData: MusicReturnInfo): Int {
        var pos = -1
        for (index in 0 until _itemArray.size) {
            val item = _itemArray[index]
            if (item.filePath == musicData.audioFilePath) {
                item.isPlaying = true
                item.isSelected = true
                item.startOffset = musicData.startOffset
                item.length = musicData.length.toLong()
                pos = index
                _curItem = item
                notifyDataSetChanged()
                break
            }
        }
        return pos
    }

    fun onPause() {
        _curItem?.let {
            it.isPlaying = false
            notifyDataSetChanged()
        }
    }

    fun setOffAll() {
        _curItem?.let {
            it.isPlaying = false
            it.isSelected = false
            notifyDataSetChanged()
        }
    }

    fun setGenders(selectedGender: String?) {
        this.choosenGender = selectedGender.orEmpty()
    }

    interface MusicCallback {
        fun onClickItem(audioModel: AudioModel)
        fun onClickUse(audioModel: AudioModel)
        fun onClickPlay(isPlay: Boolean, audioModel: AudioModel)
        fun onChangeStart(startOffsetMilSec: Int, lengthMilSec: Int)
        fun onChangeEnd(lengthMilSec: Int)

        fun onClickDownload(audioModel: AudioModel)
    }

}