package com.slideshowmaker.slideshow.modules.rate

import android.content.Context
import android.content.SharedPreferences
import com.slideshowmaker.slideshow.VideoMakerApplication

class RatingManager {
    private val preferenceName = "RatingPreference"
    private val ratedKey = "Rated"
    private val timeShowRatingKey = "TimeShowRating"
    private val videoRenderedKey = "VideoRendered"
    private val lowStarKey = "LowStar"
    private val sharePreference:SharedPreferences
    private val context = VideoMakerApplication.getContext()

    companion object {
        private var instance:RatingManager? =null
        fun getInstance():RatingManager{
            if(instance == null) {
                instance = RatingManager()
            }
            return instance!!
        }
    }

    init {
        sharePreference = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }


    fun setRated() {
        sharePreference.edit().apply {
            putBoolean(ratedKey, true)
            apply()
        }
    }

    fun isRated():Boolean = sharePreference.getBoolean(ratedKey, false)

    fun setTimeShowRating(timeStamp:Long) {
        sharePreference.edit().apply {
            putLong(timeShowRatingKey, System.currentTimeMillis()+timeStamp)
            apply()
        }
    }

    fun getTimeShowRating():Long = sharePreference.getLong(timeShowRatingKey, -1)

    fun canShowRate() :Boolean{
        if(isRated()) return false
        val timeShowRate = getTimeShowRating()
        if(timeShowRate < 0) return true
        val deltaTimeMs = System.currentTimeMillis()-timeShowRate
        return deltaTimeMs >= 0
    }

/*    fun increaseVideoRendered() {
        var number = mSharePreference.getInt(mVideoRenderedKey,0)
        ++number
        mSharePreference.edit().apply {
            putInt(mVideoRenderedKey, number)
            apply()
        }
    }*/

    fun getNumberVideoRendered():Int = sharePreference.getInt(videoRenderedKey, 0)

    fun resetNumberVideoRendered() {
        sharePreference.edit().apply {
            putInt(videoRenderedKey, 0)
            apply()
        }
    }

/*    fun setRateLowStar() {
        mSharePreference.edit().apply {
            putBoolean(mLowStarKey, true)
            apply()
        }
    }

    fun isRateLowStar():Boolean = mSharePreference.getBoolean(mLowStarKey, false)*/
}