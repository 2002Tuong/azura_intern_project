package com.slideshowmaker.slideshow.data.local

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.os.bundleOf
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import com.slideshowmaker.slideshow.data.response.MediaModel
import com.slideshowmaker.slideshow.data.response.PhotoAlbum
import com.slideshowmaker.slideshow.data.response.SnapImage
import timber.log.Timber
import java.io.File
import java.lang.NullPointerException

private val imageProjection = arrayOf(
    MediaStore.Images.Media._ID,
    MediaStore.Images.Media.DISPLAY_NAME,
    MediaStore.Images.Media.DATE_TAKEN,
    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
    MediaStore.Images.Media.MIME_TYPE,
    MediaStore.Images.Media.DATA,
)

private val albumProjection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Images.Media.BUCKET_ID,
    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
)


private val videoProjection = arrayOf(
    MediaStore.Video.Media._ID,
    MediaStore.Video.Media.DISPLAY_NAME,
    MediaStore.Video.Media.DATE_TAKEN,
    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    MediaStore.Video.Media.MIME_TYPE,
    MediaStore.Video.Media.DATA,
    MediaStore.Video.Media.DURATION
)

private val videoAlbumProjection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Video.Media.BUCKET_ID,
    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
)


internal fun Context.createCursor(
    album: String,
    limit: Int,
    offset: Int,
    mediaKind: MediaType
): Cursor? {
    val typeBucketName = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME
        MediaType.PHOTO -> MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
    }
    val mimeType = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.VideoColumns.MIME_TYPE
        MediaType.PHOTO -> MediaStore.Images.ImageColumns.MIME_TYPE
    }

    val typeContentUri = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        MediaType.PHOTO -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    val typeDateAdded = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.Media.DATE_ADDED
        MediaType.PHOTO -> MediaStore.Images.Media.DATE_ADDED
    }


    val typeProjections = when (mediaKind) {
        MediaType.VIDEO -> videoProjection
        MediaType.PHOTO -> imageProjection
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val bundle = bundleOf(
            ContentResolver.QUERY_ARG_OFFSET to offset,
            ContentResolver.QUERY_ARG_LIMIT to limit,
            ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(typeDateAdded),
            ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
        )


        if (album != PhotoAlbum.ID_ALL) {
            if (album.isEmpty()) {
                val selectionQuery =
                    "$typeBucketName IS NULL AND $mimeType != ?"
                bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selectionQuery)
                bundle.putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty()),
                )
            } else {
                val selection =
                    "$typeBucketName = ? AND $mimeType != ?"
                bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                bundle.putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(
                        album,
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty(),
                    ),
                )
            }
        } else {
            val selectionQuery =
                "$mimeType != ?"
            bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selectionQuery)
            bundle.putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff")),
            )
        }

        contentResolver.query(
            typeContentUri,
            typeProjections,
            bundle,
            null,
        )
    } else {
        var selectionQuery: String? = null
        var selectionArgs: Array<String>? = null
        if (album != PhotoAlbum.ID_ALL) {
            if (album.isEmpty()) {
                selectionQuery =
                    "${typeBucketName} IS NULL AND ${mimeType} != ?"
                selectionArgs = arrayOf(
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty(),
                )
            } else {
                selectionQuery =
                    "${typeBucketName} = ? AND ${mimeType} != ?"
                selectionArgs = arrayOf(
                    album,
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty(),
                )
            }
        }
        contentResolver.query(
            typeContentUri,
            typeProjections,
            selectionQuery,
            selectionArgs,
            "${typeDateAdded} DESC LIMIT $limit OFFSET $offset",
            null,
        )
    }
}

internal fun Context.fetchPagePicture(
    album: String,
    limit: Int,
    offset: Int,
    mediaKind: MediaType = MediaType.PHOTO
): List<SnapImage> {
    Timber.d("Thread is ${Thread.currentThread().name}")
    val pictureList = ArrayList<SnapImage>()
    val cursor = createCursor(album, limit, offset, mediaKind)
    val projection = when (mediaKind) {
        MediaType.VIDEO -> videoProjection
        MediaType.PHOTO -> imageProjection
    }
    val uri = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        MediaType.PHOTO -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    cursor?.use {
        val idColumnIndex = it.getColumnIndexOrThrow(projection[0])
        val displayNameColumnIndex = it.getColumnIndexOrThrow(projection[1])
        val dateTakenColumnIndex = it.getColumnIndexOrThrow(projection[2])
        val bucketDisplayNameIndex = it.getColumnIndexOrThrow(projection[3])
        val mimeTypeColumnIndex = it.getColumnIndexOrThrow(projection[4])
        val filePathColumnIndex = it.getColumnIndexOrThrow(projection[5])
        val durationColumnIndex =
            if (mediaKind == MediaType.VIDEO) it.getColumnIndex(projection[6]) else -1

        while (it.moveToNext()) {
            val id = it.getLong(idColumnIndex)
            val dateTaken = it.getLongOrNull(dateTakenColumnIndex)
            val displayName = it.getStringOrNull(displayNameColumnIndex).orEmpty()
            val filePath = it.getStringOrNull(filePathColumnIndex).orEmpty()
            val contentUri = ContentUris.withAppendedId(
                uri,
                id,
            )
            val folderName = it.getStringOrNull(bucketDisplayNameIndex).orEmpty()
            val duration = it.getLongOrNull(durationColumnIndex) ?: 0L
            Timber.d("Image $displayName mime type ${it.getStringOrNull(mimeTypeColumnIndex)}")
            if (File(filePath).exists()) {
                if (duration > 5000 || mediaKind == MediaType.PHOTO) {
                    pictureList.add(
                        SnapImage(
                            contentUri,
                            dateTaken,
                            displayName,
                            id,
                            folderName,
                            filePath
                        ),
                    )
                }
            }
        }
    }
    cursor?.close()
    return pictureList
}

internal fun Context.fetchPictureAlbum(mediaKind: MediaType = MediaType.PHOTO): List<PhotoAlbum> {
    var photoAlbum = listOf<PhotoAlbum>()
    val uri = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        MediaType.PHOTO -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    val projections = when (mediaKind) {
        MediaType.VIDEO -> videoAlbumProjection
        MediaType.PHOTO -> albumProjection
    }
    val selection = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.Media.SIZE
        MediaType.PHOTO -> MediaStore.Images.Media.SIZE
    }
    val mimeType = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.Media.MIME_TYPE
        MediaType.PHOTO -> MediaStore.Images.Media.MIME_TYPE
    }
    val sortBy = when (mediaKind) {
        MediaType.VIDEO -> MediaStore.Video.Media.DATE_ADDED
        MediaType.PHOTO -> MediaStore.Images.Media.DATE_ADDED
    }
    val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentResolver.query(
            uri,
            projections,
            null, null,
            "${sortBy} DESC",
            null,
        )
    } else {
        contentResolver.query(
            uri,
            projections,
            mimeType + " != ?",
            arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty()),
            "${sortBy} DESC",
            null,
        )
    }
    cursor?.use { it ->
        val totalImageList = generateSequence { if (it.moveToNext()) cursor else null }
            .map { getImage(it, mediaKind) }
            .filterNotNull()
            .toList()
        photoAlbum = totalImageList
            .groupBy { it.albumName }
            .toSortedMap { album1, album2 ->
                album1.compareTo(album2, true)
            }
            .map { entry ->
                PhotoAlbum(
                    entry.key,
                    entry.key.ifEmpty { getString(R.string.image_picker_section_storage) },
                    entry.value.firstOrNull()?.uri,
                    entry.value.count(),
                )
            }.toList()
    }
    val album = PhotoAlbum(
        PhotoAlbum.ID_ALL,
        getString(R.string.image_picker_section_all_photos),
        photoAlbum.firstOrNull()?.thumbnailUri,
        photoAlbum.sumOf { it.imageCount },
    )
    return listOf(album).plus(photoAlbum)
}

private fun getImage(cursor: Cursor, mediaKind: MediaType): MediaModel? =
    try {
        val showName = when (mediaKind) {
            MediaType.VIDEO -> MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            MediaType.PHOTO -> MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        }
        cursor.run {
            val folderName =
                getStringOrNull(getColumnIndex(showName)).orEmpty()
            val mediaUri = getMediaUri(mediaKind)
            val datedAddedSecond =
                getLongOrNull(getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)) ?: 0L
            return MediaModel(folderName, mediaUri, datedAddedSecond)
        }
    } catch (exception: Exception) {
        Timber.e("Unable to getImage from cursor", exception)
        null
    }

@Throws(NullPointerException::class)
private fun Cursor.getMediaUri(mediaKind: MediaType): Uri {
    val id = getLongOrNull(getColumnIndex(MediaStore.MediaColumns._ID))
        ?: throw NullPointerException()
    if (mediaKind == MediaType.VIDEO)
        return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
    return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
}

private fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}
