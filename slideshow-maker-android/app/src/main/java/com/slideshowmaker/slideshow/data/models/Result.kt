package com.slideshowmaker.slideshow.data.models

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val errorCode: Int,
        val exception: Exception,
        val statusCode: Int? = null,
        val retryAfter: Long? = null,
    ) : Result<Nothing>()
}
