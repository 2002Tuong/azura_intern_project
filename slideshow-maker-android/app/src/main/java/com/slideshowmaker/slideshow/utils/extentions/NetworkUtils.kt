package com.slideshowmaker.slideshow.utils.extentions

import com.slideshowmaker.slideshow.data.models.Result
import kotlinx.coroutines.Deferred
import retrofit2.Response

suspend fun <T : Any> safeApiCall(call: suspend () -> Deferred<Response<T>>): Result<T> {
    try {
        val res = call.invoke().await()
        if (res.isSuccessful) {
            res.body()?.let { data ->
                return Result.Success(data)
            } ?: return Result.Error(res.code(), Exception(res.message()))
        } else {
            return Result.Error(res.code(), Exception(res.message()))
        }
    } catch (e: Exception) {
        return Result.Error(0, e)
    }
}
