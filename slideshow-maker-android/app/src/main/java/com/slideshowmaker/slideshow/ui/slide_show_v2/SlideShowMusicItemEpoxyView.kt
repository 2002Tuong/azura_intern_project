package com.slideshowmaker.slideshow.ui.slide_show_v2

import android.view.View
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.view.isVisible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.utils.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.epoxy_view_slideshow_music_item)
abstract class SlideShowMusicItemEpoxyView :
    EpoxyModelWithHolder<SlideShowMusicItemEpoxyView.Holder>() {

    @EpoxyAttribute
    lateinit var item: AudioInfo

    @EpoxyAttribute
    var selected: Boolean = false

    @EpoxyAttribute
    var playing: Boolean = false

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var toggleClickListener: CompoundButton.OnCheckedChangeListener? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var trimClickListener: View.OnClickListener? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var playListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.rbSelectMusic.isChecked = selected
        holder.tvMusicName.text = item.musicName
        holder.ibTrim.isVisible = selected
        holder.ibPlay.setImageResource(if (playing && selected) R.drawable.icon_pause_circle else R.drawable.icon_play_circle)
        holder.ibPlay.setOnClickListener(playListener)
        holder.ibTrim.setOnClickListener(trimClickListener)
        holder.rbSelectMusic.setOnCheckedChangeListener(toggleClickListener)
    }

    class Holder : KotlinEpoxyHolder() {
        val rbSelectMusic: RadioButton by bind(R.id.rbSelectMusic)
        val tvMusicName: TextView by bind(R.id.tvMusicName)
        val ibTrim: ImageButton by bind(R.id.ibTrim)
        val ibPlay: ImageButton by bind(R.id.ibPlay)
    }
}