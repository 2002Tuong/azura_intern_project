package com.parallax.hdvideo.wallpapers.data.model

// code = -1
// code > 0 api response
data class FailedResponse(
    val code: Int,
    override val cause: Throwable?,
    override val message: String?,
    val online: Boolean = true,
    val url: String? = null): Exception() {
}