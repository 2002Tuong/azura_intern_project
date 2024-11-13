package snapedit.app.remove.data

import android.net.Uri

data class Image(
    val uri: Uri,
    internal val dateTaken: Long?,
    val displayName: String?,
    internal val id: Long?,
    internal val folderName: String?,
)
