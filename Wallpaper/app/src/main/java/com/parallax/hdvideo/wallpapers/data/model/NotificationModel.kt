package com.parallax.hdvideo.wallpapers.data.model

import com.google.gson.annotations.SerializedName
import com.parallax.hdvideo.wallpapers.utils.AppConstants.PATTERN_DATE
import com.parallax.hdvideo.wallpapers.utils.AppConstants.PATTERN_DATE_TIME
import java.text.SimpleDateFormat
import java.util.*

class NotificationModel {
    @SerializedName("id")
    var id: String = ""
    @SerializedName("name")
    var name: String = ""
    @SerializedName("description")
    var description: String = ""
    @SerializedName("objectId")
    var objectId: String = ""
    @SerializedName("countries")
    var countries: String = ""
    @SerializedName("time")
    var time = ""
    @SerializedName("timeOrigin")
    var timeOrigin = ""
    @SerializedName("type")
    var type = "normal"

    fun getDate(): Date? {
        try {
            val pattern: String = if (time.contains(":")) PATTERN_DATE_TIME else PATTERN_DATE
            val simpleFormatter = SimpleDateFormat(pattern, Locale.getDefault())
            return simpleFormatter.parse(time)
        } catch (e: Exception) {
        }
        return null
    }

    fun removeHours() {
        val dateNow = getDate()
        if (dateNow != null) {
            val formatter = SimpleDateFormat(PATTERN_DATE, Locale.getDefault())
            time = formatter.format(dateNow)
        }
    }

    fun getCalendar(): Calendar? {
        val dateNow = getDate()
        if (dateNow != null) {
            val calendar = Calendar.getInstance()
            calendar.time = dateNow
            return calendar
        }
        return null
    }
}