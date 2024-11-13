package com.slideshowmaker.slideshow.ui.select_music

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.adapter.AudioListAdapter
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.data.models.MusicReturnInfo
import com.slideshowmaker.slideshow.databinding.FragmentSelectMusicBinding
import com.slideshowmaker.slideshow.models.AudioModel
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpeg
import com.slideshowmaker.slideshow.models.ffmpeg.FFmpegCmd
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayer
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.ui.dialog.ConfirmPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.SlideshowMakerDialogFragment
import com.slideshowmaker.slideshow.ui.music_picker.MusicPickerViewModel
import com.slideshowmaker.slideshow.ui.pick_media.PickMediaViewModel
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.extentions.launchAndRepeatOnLifecycleStarted
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import kotlinx.android.synthetic.main.fragment_select_music.musicListView
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import timber.log.Timber

class SelectMusicFragment : Fragment(), KodeinAware {
    private var _binding: FragmentSelectMusicBinding? = null


    private val layoutBinding get() = _binding!!

    private val remoteMusicViewModel: SelectMusicViewModel by instance()
    private val localMusicViewModel: PickMediaViewModel by instance()
    private val musicPlayer: MusicPlayer by instance()

    override val kodein by closestKodein()
    private val musicType: MusicType by lazy {
        MusicType.valueOf(
            arguments?.getString(ARG_MUSIC_TYPE_KEY) ?: MusicType.ONLINE.name
        )
    }

    private var useValid = true
    private val audioListAdapter = AudioListAdapter(object : AudioListAdapter.MusicCallback {
        override fun onClickItem(audioModel: AudioModel) {
            if (musicPlayer.getOutMusic().audioFilePath != audioModel.filePath)
                musicPlayer.changeMusic(audioModel.filePath)
            else
                musicPlayer.changeState()
        }

        override fun onClickUse(audioModel: AudioModel) {
            val out = musicPlayer.getOutMusic()
            if (out.length == 0) {
                performUseMusic(
                    audioModel,
                    audioModel.filePath,
                    0,
                    audioModel.length,
                    audioModel.fileType
                )
                return
            }
            if (out.length < 10000) {
                showToast(getString(R.string.toast_minimum_time_is_10s))
            } else {
                if (!useValid) {
                    return
                }
                useValid = false
                performUseMusic(
                    audioModel,
                    out.audioFilePath,
                    out.startOffset.toLong(),
                    out.startOffset + out.length.toLong(),
                    audioModel.fileType
                )
            }
        }

        override fun onClickPlay(isPlaying: Boolean, audioModel: AudioModel) {
            if (isPlaying) {
                musicPlayer.changeMusic(audioModel.filePath)
            } else {
                musicPlayer.changeState()
            }
        }

        override fun onChangeStart(
            startOffsetMilSec: Int, lengthMilSec: Int
        ) {
            musicPlayer.changeStartOffset(startOffsetMilSec)
            musicPlayer.changeLength(lengthMilSec)
        }

        override fun onChangeEnd(lengthMilSec: Int) {
            musicPlayer.changeLength(lengthMilSec)
        }

        override fun onClickDownload(audioModel: AudioModel) {
            /*if (RemoteConfigRepository.isVersionAdsEnable) {
                showWatchAdsDialog {
                    remoteMusicViewModel.downloadMusic(
                        audioDataModel.audioFilePath, audioDataModel.fileId
                    )
                }
            } else {
                remoteMusicViewModel.downloadMusic(
                    audioDataModel.audioFilePath, audioDataModel.fileId
                )
            }*/

            remoteMusicViewModel.downloadMusic(
                audioModel.filePath, audioModel.fileId
            )
        }

    })

    private fun showWatchAdsDialog(onComplete: () -> Unit) {
        SlideshowMakerDialogFragment.Builder()
            .setMessage(getString(R.string.watch_ads_to_download_music_label))
            .setPrimaryButton(object : SlideshowMakerDialogFragment.Builder.Button {
                override val label: String
                    get() = getString(R.string.regular_ok)

                override fun onClickListener(dialog: Dialog?) {
                    dialog?.dismiss()
                    onComplete()
                }

            }).setSecondaryButton(object : SlideshowMakerDialogFragment.Builder.Button {
                override val label: String
                    get() = getString(R.string.regular_cancel)

                override fun onClickListener(dialog: Dialog?) {
                    dialog?.dismiss()
                }


            }).build().show(childFragmentManager, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectMusicBinding.inflate(inflater, container, false)
        val viewRoot = layoutBinding.root
        return viewRoot
    }

    private var selectedSex = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicListView.adapter = audioListAdapter
        musicListView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        if (musicType == MusicType.ONLINE) {
            val sexList = RemoteConfigRepository.categories.orEmpty().distinct()
            selectedSex = sexList.firstOrNull().orEmpty()
            layoutBinding.rvMusicGender.withModels {
                sexList.forEach {
                    MusicGenderEpoxyView_().id(it).gender(it).selected(it == selectedSex)
                        .toggleClickListener { model, parentView, clickedView, position ->
                            musicPlayer.pause()
                            selectedSex = model.gender()
                            requestModelBuild()
                            listen(remoteMusicViewModel.downloadedMusic.value)
                        }.addTo(this)
                }
            }
            launchAndRepeatOnLifecycleStarted {
                remoteMusicViewModel._downloadMusicsState.collect {
                    listen(it)
                }
            }

            launchAndRepeatOnLifecycleStarted {
                remoteMusicViewModel._downloadingProgressState.collect {
                    if (it != null) {
                        if ((activity as? BaseActivity)?.isProgressShow.orFalse()) {
                            (activity as? BaseActivity)?.updateProgressDialogProgress(it)
                        } else {
                            (activity as? BaseActivity)?.showProgressDialog(getString(R.string.download_music))
                        }
                    } else {
                        (activity as? BaseActivity)?.hideProgressDialog()
                    }
                }
            }
            launchAndRepeatOnLifecycleStarted {
                remoteMusicViewModel.downloadedMusic.collect {
                    listen(it)
                }
            }
            //remoteMusicViewModel.getDownloadedMusics()
        } else {
            observeLocalMusic()
        }

        launchAndRepeatOnLifecycleStarted {
            remoteMusicViewModel.errorMessageState.collect {
                if (it != null) {
                    ConfirmPopupFragment.Builder().setTitle(getString(it?.first ?: 0))
                        .setContent(getString(it?.second ?: 0)).build()
                        .show(childFragmentManager, "ConfirmDialogFragment")
                    remoteMusicViewModel.clearError()
                }
            }
        }
    }

    private fun onSearch(query: String) {
        onPauseMusic()
        audioListAdapter.setOffAll()
        val audioResult = ArrayList<AudioInfo>()
        for (item in allAudioList) {
            Logger.e("music name = ${item.musicName}")
            if (item.musicName.toLowerCase().contains(query.toLowerCase())) {
                audioResult.add(item)
            }
        }
        audioListAdapter.setAudioDataList(audioResult)
    }

    private val allAudioList = ArrayList<AudioInfo>()

    private fun observeLocalMusic() {
        localMusicViewModel.localStorageData.getAllAudio()
        localMusicViewModel.localStorageData.audioInfoResponse.observe(viewLifecycleOwner) {
            audioListAdapter.setAudioDataList(it)
            allAudioList.addAll(it)
        }
    }

    private fun listen(downloadedMusic: List<MusicPickerViewModel.DownloadMusic>) {
        val remoteAudio = RemoteConfigRepository.audioConfigs
        val remoteAudios = remoteAudio.orEmpty()
            .filter {
                it.category.orEmpty().split(",")
                    .contains(selectedSex) || selectedSex.isBlank()
            }
            .map {
                AudioInfo(
                    it.audioUrl.orEmpty(),
                    it.name.orEmpty(),
                    "audio/mp3",
                    0L,
                    it.id.orEmpty(),
                    it.thumbnailUrl.orEmpty()
                )
            }.toMutableList()

        downloadedMusic.forEach { downloaded ->
            val item =
                remoteAudios.firstOrNull { it.filePath.contains(downloaded.id) && it.duration == 0L }
            val itemIndex =
                remoteAudios.indexOfFirst { it.filePath.contains(downloaded.id) && it.duration == 0L }
            if (item != null) {
                val newItem = item.copy(
                    filePath = downloaded.filePath, duration = downloaded.duration.toLong()
                )
                remoteAudios.removeAt(itemIndex)
                remoteAudios.add(itemIndex, newItem)
            }
        }

        val outputAudios = ArrayList(remoteAudios)
        audioListAdapter.setAudioDataList(outputAudios)
        allAudioList.clear()
        allAudioList.addAll(remoteAudios)
    }

    private fun performUseMusic(
        audioModel: AudioModel,
        inputAudioPath: String,
        startOffset: Long,
        endOffset: Long,
        fileType: String
    ) {
        musicPlayer.pause()
        if (!isAdded) return
        (activity as? BaseActivity)?.showLoadingDialog()
        Thread {
            val outputAudioPath: String// = FileUtils.getTempAudioOutPutFile("m4a")
            val extension = "mp3"
            Logger.e("ex = $extension")
            outputAudioPath = if (extension != "m4a") {
                FileHelper.getTempAudioOutPutFile(extension)
            } else {
                FileHelper.getTempAudioOutPutFile("mp4")
            }

            TrackingFactory.Music.useMusic(inputAudioPath, startOffset, endOffset).track()
            Logger.e("out mp3 = $outputAudioPath")
            val ffmpeg =
                FFmpeg(FFmpegCmd.trimAudio(inputAudioPath, startOffset, endOffset, outputAudioPath))
            TrackingFactory.Common
            ffmpeg.runCmd {
                try {

                    val musicReturnInfo = MusicReturnInfo(
                        inputAudioPath,
                        outputAudioPath,
                        startOffset.toInt(),
                        (endOffset.toInt() - startOffset.toInt()) / 1000,
                        audioModel.fileId,
                        audioModel.name
                    )
                    requireActivity().runOnUiThread {
                        val returnIntent = Intent()
                        Bundle().apply {
                            putSerializable(
                                SelectMusicActivity.MUSIC_RETURN_DATA_KEY, musicReturnInfo
                            )
                            putBoolean("download_music", remoteMusicViewModel.hasDownloadedFinished)
                            returnIntent.putExtra("bundle", this)
                        }
                        activity?.setResult(Activity.RESULT_OK, returnIntent)
                        (activity as? BaseActivity)?.dismissLoadingDialog()
                        activity?.finish()
                    }
                } catch (e: Exception) {
                    requireActivity()?.runOnUiThread {
                        (activity as? BaseActivity)?.dismissLoadingDialog()
                        useValid = true
                        Timber.e(e)
                        Firebase.crashlytics.recordException(e)
                        showToast(getString(R.string.error_try_another_music_file))
                    }

                }

            }
        }.start()


    }

    fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }

    }

    private fun onPauseMusic() {
        musicPlayer.pause()
        audioListAdapter.onPause()
    }

    override fun onPause() {
        super.onPause()
        onPauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class MusicType(val type: String) {
        LOCAL("local"), ONLINE("online")
    }

    companion object {
        private const val ARG_MUSIC_TYPE_KEY = "MusicType"
        fun newInstance(musicType: MusicType): SelectMusicFragment {
            val args = Bundle()
            args.putString(ARG_MUSIC_TYPE_KEY, musicType.name)
            val fragment = SelectMusicFragment()
            fragment.arguments = args
            return fragment
        }
    }

}