package com.slideshowmaker.slideshow.ui.music_picker

import com.airbnb.epoxy.EpoxyController
import com.slideshowmaker.slideshow.data.response.ItemAudio
import com.slideshowmaker.slideshow.utils.extentions.orFalse

class MusicPickerController(
) : EpoxyController() {
    var progress: Int = 0
    var musics: List<ItemAudio> = emptyList()
    var downloadMusic: List<MusicPickerViewModel.DownloadMusic> = emptyList()
    var listener: Listener? = null
    var playingMusicId: String = ""
    var playingState = PlayerState.PREPARING
    var filter: String = ""
    var trimId: String = ""
    var start: Int? = null
    var end: Int? = null
    override fun buildModels() {
        musics.filter { it.name?.contains(filter).orFalse() }.forEach {
            MusicItemEpoxyModel_()
                .id(it.id)
                .item(it)
                .selectedMusic(it.id == playingMusicId)
                .trimClickListener { model, _, _, _ ->
                    if (trimId == model.item().id) {
                        trimId = ""
                        requestModelBuild()
                        return@trimClickListener
                    } else {
                        start = null
                        end = null
                    }
                    if (playingMusicId != model.item().id) {
                        listener?.stopMusic()
                        playingMusicId = model.item().id.orEmpty()
                        listener?.playMusic(model.item())
                    }
                    trimId = model.item().id.orEmpty()
                    requestModelBuild()
                }
                .duration(
                    downloadMusic.firstOrNull { download -> download.id == it.id }?.duration ?: 0
                )
                .startDuration(start ?: 0)
                .endDuration(
                    end ?: downloadMusic.firstOrNull { download -> download.id == it.id }?.duration
                    ?: 0
                )
                .onSeekbarChanged { id, start, end -> }
                .trimEnabled(it.id == trimId)
                .playerState(playingState)
                .progress(progress)
                .downloaded(downloadMusic.any { downloaded -> downloaded.downloaded && downloaded.id == it.id })
                .downloadProgress(
                    downloadMusic.firstOrNull { item -> item.id == it.id }?.progress ?: -1
                )
                .downloadClickListener { model, _, _, _ ->
                    listener?.downloadMusic(model.item)
                }
                .playMusicClickListener { model, _, _, _ ->
                    if (model.item().id != trimId) {
                        end = null
                        start = null
                    }
                    if (playingMusicId != model.item().id) {
                        listener?.stopMusic()
                        playingMusicId = model.item().id.orEmpty()
                        listener?.playMusic(model.item())
                    } else {
                        listener?.stopMusic()
                    }
                }.useMusicClickListener { model, _, _, _ ->
                    if (trimId != model.item().id) {
                        listener?.useMusic(
                            downloadMusic.firstOrNull { downloaded -> downloaded.id == model.item().id }?.filePath.orEmpty(),
                            null, null
                        )
                    } else {
                        listener?.useMusic(
                            downloadMusic.firstOrNull { downloaded -> downloaded.id == model.item().id }?.filePath.orEmpty(),
                            start, end
                        )
                    }
                }
                .onSeekbarChanged { id, start, end ->
                    this.start = start
                    this.end = end
                    listener?.changeMusicRange(start, end)
                }
                .addTo(this)
        }
    }

    interface Listener {
        fun downloadMusic(snapMusic: ItemAudio)
        fun playMusic(snapMusic: ItemAudio)
        fun stopMusic()
        fun useMusic(filePath: String, start: Int?, end: Int?)
        fun changeMusicRange(start: Int, end: Int)
        fun toggleMusic()
    }
}