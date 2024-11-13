package com.slideshowmaker.slideshow.data.models

import com.slideshowmaker.slideshow.data.models.enum.MediaType

data class MediaInfo(val dateAdded:Long, val filePath:String = "", val fileName:String="", val mediaKind: MediaType = MediaType.PHOTO, val folderId:String="", val folderName:String="", val duration:Long=0)