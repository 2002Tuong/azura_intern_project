package com.example.claptofindphone.presenter.common

import android.content.res.Resources
import android.util.TypedValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@FlowPreview
@ExperimentalCoroutinesApi
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var latestEmissionTime = 0L
    collect { upstream ->
        val currTime = System.currentTimeMillis()
        val shouldEmit = currTime - latestEmissionTime > windowDuration
        if (shouldEmit)
        {
            latestEmissionTime = currTime
            emit(upstream)
        }
    }
}


val Number.toPx get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics)