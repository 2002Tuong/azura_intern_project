package com.slideshowmaker.slideshow.data.response

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class SnapImage(
    val uri: Uri,
    internal val dateTaken: Long?,
    val displayName: String?,
    internal val id: Long?,
    internal val folderName: String?,
    internal val filePath: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeValue(dateTaken)
        parcel.writeString(displayName)
        parcel.writeValue(id)
        parcel.writeString(folderName)
        parcel.writeString(filePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SnapImage> {
        override fun createFromParcel(parcel: Parcel): SnapImage {
            return SnapImage(parcel)
        }

        override fun newArray(size: Int): Array<SnapImage?> {
            return arrayOfNulls(size)
        }
    }
}
