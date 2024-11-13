package com.slideshowmaker.slideshow.modules.local_storage

import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.data.models.MediaInfo
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File

class LocalStorageDataImpl :
    LocalStorageData {

    override val audioInfoResponse = MutableLiveData<ArrayList<AudioInfo>>()

    override val mediaDataResponse = MutableLiveData<ArrayList<MediaInfo>>()


    override fun getAllAudio() {
        val audioList = arrayListOf<AudioInfo>()
        Observable.fromCallable<ArrayList<AudioInfo>> {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val orderBy = MediaStore.Audio.Media.DATE_ADDED
            val selectionMusic = MediaStore.Audio.Media.IS_MUSIC + " != 0"
            val cursor: Cursor =
                VideoMakerApplication.getContext().contentResolver.query(
                    uri,
                    null,
                    selectionMusic, null, "$orderBy DESC"
                )!!
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val audioName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                        ?: ""
                val mineType =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)) ?: ""
                val duration = try {
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                } catch (e: Exception) {
                    continue
                }
                /*val duration =
                    cursor.getLong(cursor.getColumnIndex("duration"))*/
                if (filePath.toLowerCase().contains(".m4a") || filePath.toLowerCase()
                        .contains(".mp3")
                ) {
                    if (duration > 10000) {
                        audioList.add(AudioInfo(filePath, audioName, mineType, duration/1000L, ""))
                    }
                }
            }
            cursor.close()
            return@fromCallable audioList
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<AudioInfo>> {
                override fun onNext(t: ArrayList<AudioInfo>) {
                    audioInfoResponse.postValue(t)
                }

                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })
    }


    override fun getAllMedia(mediaKind: MediaType) {
        when (mediaKind) {
            MediaType.VIDEO -> getAllVideos()
            MediaType.PHOTO -> getAllPhoto()
        }
    }


    private fun getAllPhoto() {
        val mediaList = arrayListOf<MediaInfo>()
        Observable.fromCallable<ArrayList<MediaInfo>> {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val orderBy = MediaStore.Images.Media.DATE_ADDED
            val cursor: Cursor =
                VideoMakerApplication.getContext().contentResolver.query(
                    uri,
                    null,
                    null, null, "$orderBy DESC"
                )!!
            var file: File
            while (cursor.moveToNext()) {


                val dateAdded =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                val path =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)) ?: ""
                val folderContainName = File(path)?.parentFile?.name ?: ""
                if (path.toLowerCase().contains(".tif") || path.toLowerCase()
                        .contains(".psd") || path.toLowerCase().contains(".ai")
                ) continue
                if (File(path).length() > 100)
                    if (!path.toLowerCase().contains(".gif") && !path.toLowerCase()
                            .contains("!\$&welcome@#image")
                    ) {
                        file = File(path)

                        if (file.exists()) {
                            val folderContainId = file.parentFile?.absolutePath ?: ""
                            mediaList.add(
                                MediaInfo(
                                    dateAdded * 1000,
                                    path,
                                    file.name,
                                    MediaType.PHOTO,
                                    folderContainId,
                                    folderContainName
                                )
                            )
                        }
                    }

            }
            cursor.close()
            return@fromCallable mediaList
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<MediaInfo>> {
                override fun onNext(t: ArrayList<MediaInfo>) {
                    mediaDataResponse.postValue(t)
                }

                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })
    }

    private fun getAllVideos() {
        val mediaList = arrayListOf<MediaInfo>()
        Observable.fromCallable<ArrayList<MediaInfo>> {

            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val orderBy = MediaStore.Video.Media.DATE_ADDED
            val cursor: Cursor =
                VideoMakerApplication.getContext().contentResolver.query(
                    uri,
                    null,
                    null, null, "$orderBy DESC"
                )!!
            var file: File

            // cursor.moveToFirst()

            while (cursor.moveToNext()) {

                val dateAdded =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))

                val path =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)) ?: ""
                val folderContainName = File(path).parentFile?.name ?: ""
                val duration = try {
                    cursor.getLong(cursor.getColumnIndex("duration"))
                } catch (e: Exception) {
                    continue
                }

                if (!path.toLowerCase().contains(".mp4")) continue
                file = File(path)
                if (duration > 1000)
                    if (file.exists()) {
                        try {
                            val folderContainId = file.parentFile?.absolutePath ?: ""
                            mediaList.add(
                                MediaInfo(
                                    dateAdded * 1000,
                                    path,
                                    file.name,
                                    MediaType.VIDEO,
                                    folderContainId,
                                    folderContainName,
                                    duration
                                )
                            )
                        } catch (e: java.lang.Exception) {
                            continue
                        }

                    }
            }
            cursor.close()
            return@fromCallable mediaList
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ArrayList<MediaInfo>> {
                override fun onNext(t: ArrayList<MediaInfo>) {
                    mediaDataResponse.postValue(t)
                }

                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {}
            })
    }


}