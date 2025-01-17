package com.example.videoart.batterychargeranimation.helper

import android.annotation.SuppressLint
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.util.Size
import java.security.InvalidParameterException


object MediaHelper {

    fun getAudioDuration(path:String):Long {

        return try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(path)
            val duration = (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?:"-1").toLong()
            duration
        } catch (e:Exception) {
            -1
        }
    }

    fun getVideoSize(videoPath:String) :Size{
        try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath)
            val rot = (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?:"-1").toInt()
            val videoWidth:Int
            val videoHeight:Int
            if (rot == 90) {
                videoWidth = (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "-1").toInt()
                videoHeight = (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
            } else {
                videoWidth = (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
                videoHeight = (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "-1").toInt()
            }
            return Size(videoWidth, videoHeight)
        } catch (e:Exception) {
            return Size(1, 1)
        }


    }

    fun getVideoBitRare(videoPath:String) :Int{
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(videoPath)
        val bitRare = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        return (bitRare ?: "-1").toInt()
    }


    fun getVideoDuration(videoPath:String) :Int{
        return try {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(videoPath)
            val duration = (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?:"-1").toInt()
            duration
        } catch (e:Exception) {
            0
        }
    }
    fun getVideoMimeType(videoPath:String) :String{
        return try {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoPath)
            val mimeType = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()
            mimeType
        } catch (e:Exception) {
           ""
        }
    }
    fun selectVideoTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val mediaFormat = extractor.getTrackFormat(i)
            if ((mediaFormat.getString(MediaFormat.KEY_MIME)?:"-1").startsWith("video/")) {
                extractor.selectTrack(i)
                return mediaFormat
            }
        }

        throw InvalidParameterException("File contains no video track")
    }

    fun selectAudioTrack(extractor: MediaExtractor): MediaFormat {
        for (i in 0 until extractor.trackCount) {
            val mediaFormat = extractor.getTrackFormat(i)
            if ((mediaFormat.getString(MediaFormat.KEY_MIME) ?: "-1").startsWith("audio/")) {
                extractor.selectTrack(i)
                return mediaFormat
            }
        }

        throw InvalidParameterException("File contains no audio track")
    }

    @SuppressLint("DefaultLocale")
    fun videoHasAudio(videoPath:String):Boolean {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(videoPath)
         try {
            val hasAudio = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) ?: ""
            if(hasAudio.toLowerCase() == "yes") return true
            return false

        } catch (e:Exception) {
            return false
        }
    }

}