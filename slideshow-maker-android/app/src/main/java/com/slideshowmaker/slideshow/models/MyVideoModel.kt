package com.slideshowmaker.slideshow.models

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MyVideoModel(val filePath: String, var dateAdded: Long = 0, val duration: Int) :
    Comparable<MyVideoModel> {

    var checked = false

    init {
        if (filePath.isNotEmpty()) {
            dateAdded = File(filePath).lastModified()
        }
    }

    override fun compareTo(other: MyVideoModel): Int = other.dateAdded.compareTo(dateAdded)

    fun fileSizeMb() = File(filePath).length() / 1024 / 1024
    fun formattedDateAdded() =
        SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault()).format(Date(dateAdded))
}