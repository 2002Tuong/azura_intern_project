package com.slideshowmaker.slideshow.ui.music_picker

import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import coil.load
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.response.ItemAudio
import com.slideshowmaker.slideshow.ui.custom.SeekBarRangedView
import com.slideshowmaker.slideshow.ui.custom.addActionListener
import com.slideshowmaker.slideshow.utils.KotlinEpoxyHolder
import com.slideshowmaker.slideshow.utils.Utils

@EpoxyModelClass(layout = R.layout.epoxy_view_music_item)
abstract class MusicItemEpoxyModel : EpoxyModelWithHolder<MusicItemEpoxyModel.Holder>() {

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var useMusicClickListener: View.OnClickListener? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var downloadClickListener: View.OnClickListener? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var playMusicClickListener: View.OnClickListener? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var trimClickListener: View.OnClickListener? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onSeekbarChanged: ((String, Int, Int) -> Unit)? = null

    @EpoxyAttribute
    lateinit var item: ItemAudio

    @EpoxyAttribute
    var playerState: PlayerState = PlayerState.PREPARING

    @EpoxyAttribute
    var selectedMusic: Boolean = false

    @EpoxyAttribute
    var progress: Int = 0

    @EpoxyAttribute
    var downloadProgress: Int = 0

    @EpoxyAttribute
    var downloaded: Boolean = false

    @EpoxyAttribute
    var trimEnabled: Boolean = false

    @EpoxyAttribute
    var startDuration: Int = 0

    @EpoxyAttribute
    var endDuration: Int = 0

    @EpoxyAttribute
    var duration: Int = 0


    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.playingProgress.isInvisible = !selectedMusic
        holder.btnPause.setOnClickListener(playMusicClickListener)
        holder.btnPause.isVisible = playerState != PlayerState.PREPARING && selectedMusic
        holder.btnPause.setImageResource(if (playerState == PlayerState.PAUSE) R.drawable.icon_play_24_white else R.drawable.icon_pause_24)

        when (playerState) {
            PlayerState.PREPARING -> holder.playingProgress.isIndeterminate = true
            PlayerState.PLAYING -> holder.playingProgress.isIndeterminate = false
            PlayerState.PAUSE -> holder.playingProgress.isIndeterminate = false
        }

        holder.layoutAudioStat.isVisible = trimEnabled && downloaded && selectedMusic
        holder.audioSeekbar.isVisible = trimEnabled && downloaded && selectedMusic
        holder.btnTrim.isVisible = downloaded
        holder.playingProgress.progress = progress

        holder.ivImage.load(item.thumbnailUrl)
        holder.tvTitle.text = item.name
        holder.useButton.isVisible = downloaded
        holder.progressDownload.progress = progress
        holder.layoutDownload.isVisible = !downloaded
        holder.useButton.setOnClickListener(useMusicClickListener)
        holder.btnTrim.setOnClickListener(trimClickListener)
        holder.ivImage.setOnClickListener(playMusicClickListener)
        holder.layoutDownload.setOnClickListener(downloadClickListener)
        holder.tvStart.text = Utils.convertSecToTimeString(startDuration)
        holder.tvEnd.text = Utils.convertSecToTimeString(duration)
        holder.audioSeekbar.setMaxValue(duration.toFloat())
        holder.audioSeekbar.setMinValue(0f)
//        holder.audioSeekbar.setSelectedMinValue(startDuration.toFloat(), false)
//        holder.audioSeekbar.setSelectedMaxValue(endDuration.toFloat(), false)
        holder.audioSeekbar.addActionListener { minValue, maxValue ->
            onSeekbarChanged?.invoke(item.id.toString(), minValue.toInt(), maxValue.toInt())
            holder.tvStart.text = Utils.convertSecToTimeString(minValue.toInt())
            holder.tvEnd.text = Utils.convertSecToTimeString(maxValue.toInt())
        }
    }

    class Holder : KotlinEpoxyHolder() {
        val playingProgress: CircularProgressIndicator by bind(R.id.playingProgress)
        val btnPause: ImageButton by bind(R.id.btnPause)
        val btnTrim: ImageButton by bind(R.id.btnTrim)
        val layoutAudioStat: LinearLayout by bind(R.id.layoutAudioStat)
        val audioSeekbar: SeekBarRangedView by bind(R.id.audioSeekbar)
        val ivImage: ImageView by bind(R.id.ivImage)
        val tvTitle: TextView by bind(R.id.tvTitle)
        val tvStart: TextView by bind(R.id.tvStart)
        val tvEnd: TextView by bind(R.id.tvEnd)
        val useButton: AppCompatButton by bind(R.id.btnUse)
        val layoutDownload: FrameLayout by bind(R.id.layoutDownload)
        val progressDownload: CircularProgressIndicator by bind(R.id.progressDownload)
        val root: LinearLayout by bind(R.id.container)
    }

    companion object {
        internal const val ID = "CameraModelView"
    }
}
