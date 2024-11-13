package com.parallax.hdvideo.wallpapers.utils

import android.util.Log
import com.google.gson.Gson
import com.parallax.hdvideo.wallpapers.BuildConfig

object Logger {

    fun d(vararg objects: Any?) {
        if (BuildConfig.DEBUG)
        Log.d("myInfo", getString(*objects))
    }

    fun e(vararg objects: Any?) {
        if (BuildConfig.DEBUG)
        Log.e("myInfo", getString(*objects))
    }

    fun i(s: String?, vararg objects: Any?) {
        if (BuildConfig.DEBUG)
        Log.i("myInfo", getString(*objects))
    }

    fun v(s: String?, vararg objects: Any?) {
        if (BuildConfig.DEBUG)
        Log.v("myInfo", getString(*objects))
    }

    fun j(vararg objects: Any?) {
        if (BuildConfig.DEBUG)
            Log.d("myInfo", getStringJson(*objects))
    }

    private fun getString(vararg objects: Any?) : String {
        return objects.foldIndexed(""){index, acc, any ->
            if (index > 0) acc.plus(" ; ").plus(any)
            else any.toString()
        }
    }

    private fun getStringJson(vararg objects: Any?) : String {
        val gson = Gson()
        return objects.foldIndexed(""){index, acc, any ->
            if (index > 0) acc.plus(" ; ").plus(gson.toJson(any))
            else gson.toJson(any)
        }
    }
}