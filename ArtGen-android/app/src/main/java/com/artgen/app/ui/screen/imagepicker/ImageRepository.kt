package com.artgen.app.ui.screen.imagepicker

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.os.bundleOf
import com.artgen.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import snapedit.app.remove.data.Image

class ImageRepository(private val context: Context) {

    fun getPicturePagingSource(album: String): ImageDataSource {
        return ImageDataSource { limit, offset ->
            fetchPagePicture(album, limit, offset)
        }
    }

    private fun fetchPagePicture(album: String, limit: Int, offset: Int): List<Image> {
        val pictures = ArrayList<Image>()
        val cursor = createCursor(album, limit, offset)
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(imageProjection[0])
            val displayNameColumn = it.getColumnIndexOrThrow(imageProjection[1])
            val dateTakenColumn = it.getColumnIndexOrThrow(imageProjection[2])
            val bucketDisplayName = it.getColumnIndexOrThrow(imageProjection[3])

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val dateTaken = it.getLongOrNull(dateTakenColumn)
                val displayName = it.getStringOrNull(displayNameColumn).orEmpty()
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id,
                )
                val folderName = it.getStringOrNull(bucketDisplayName).orEmpty()

                pictures.add(
                    Image(
                        contentUri,
                        dateTaken,
                        displayName,
                        id,
                        folderName,
                    ),
                )
            }
        }
        cursor?.close()
        return pictures
    }

    private fun createCursor(album: String, limit: Int, offset: Int): Cursor? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bundle = bundleOf(
                ContentResolver.QUERY_ARG_OFFSET to offset,
                ContentResolver.QUERY_ARG_LIMIT to limit,
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(MediaStore.Images.Media.DATE_ADDED),
                ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            )

            if (album != ImageAlbum.ID_ALL) {
                if (album.isEmpty()) {
                    val selection =
                        "${MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME} IS NULL AND ${MediaStore.Images.ImageColumns.MIME_TYPE} != ?"
                    bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                    bundle.putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty(),
                        ),
                    )
                } else {
                    val selection =
                        "${MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME} = ? AND ${MediaStore.Images.ImageColumns.MIME_TYPE} != ?"
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
                val selection =
                    "${MediaStore.Images.ImageColumns.MIME_TYPE} != ?"
                bundle.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                bundle.putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff")),
                )
            }

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                bundle,
                null,
            )
        } else {
            var selection: String? = null
            var selectionArgs: Array<String>? = null
            if (album != ImageAlbum.ID_ALL) {
                if (album.isEmpty()) {
                    selection =
                        "${MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME} IS NULL AND ${MediaStore.Images.ImageColumns.MIME_TYPE} != ?"
                    selectionArgs = arrayOf(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty(),
                    )
                } else {
                    selection =
                        "${MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME} = ? AND ${MediaStore.Images.ImageColumns.MIME_TYPE} != ?"
                    selectionArgs = arrayOf(
                        album,
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty(),
                    )
                }
            }
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset",
                null,
            )
        }
    }

    suspend fun getAlbums(): List<ImageAlbum> {
        return withContext(Dispatchers.IO) {
            fetchPictureAlbum()
        }
    }

    private fun fetchPictureAlbum(): List<ImageAlbum> {
        var albums = listOf<ImageAlbum>()
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                albumProjection,
                MediaStore.Images.Media.SIZE + " > 0 AND " + MediaStore.Images.Media.MIME_TYPE + " != ?",
                arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty()),
                "${MediaStore.Images.Media.DATE_ADDED} DESC",
                null,
            )
        } else {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                albumProjection,
                MediaStore.Images.Media.MIME_TYPE + " != ?",
                arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("tiff").orEmpty()),
                "${MediaStore.Images.Media.DATE_ADDED} DESC",
                null,
            )
        }
        cursor?.use { it ->
            val totalImageList = generateSequence { if (it.moveToNext()) cursor else null }
                .map { getImage(it) }
                .filterNotNull()
                .toList()
            albums = totalImageList
                .groupBy { it.albumName }
                .toSortedMap { album1, album2 ->
                    album1.compareTo(album2, true)
                }
                .map { entry ->
                    ImageAlbum(
                        entry.key,
                        entry.key.ifEmpty { context.getString(R.string.image_picker_section_storage) },
                        entry.value.firstOrNull()?.uri,
                        entry.value.count(),
                    )
                }.toList()
        }
        val allPhoto = ImageAlbum(
            ImageAlbum.ID_ALL,
            context.getString(R.string.image_picker_section_all_photos),
            albums.firstOrNull()?.thumbnailUri,
            albums.sumOf { it.imageCount },
        )
        return listOf(allPhoto).plus(albums)
    }

    private fun getImage(cursor: Cursor): Media? =
        try {
            cursor.run {
                val folderName =
                    getStringOrNull(getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)).orEmpty()
                val mediaUri = getMediaUri()
                val datedAddedSecond =
                    getLongOrNull(getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)) ?: 0L
                return Media(folderName, mediaUri, datedAddedSecond)
            }
        } catch (exception: Exception) {
            null
        }

    @Throws(NullPointerException::class)
    private fun Cursor.getMediaUri(): Uri {
        val id = getLongOrNull(getColumnIndex(MediaStore.MediaColumns._ID))
            ?: throw NullPointerException()
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }

    private companion object {
        private val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
        )

        private val albumProjection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        )
    }
}
