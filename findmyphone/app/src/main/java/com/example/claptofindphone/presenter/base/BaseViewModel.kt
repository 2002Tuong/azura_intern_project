package com.example.claptofindphone.presenter.base

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor() : ViewModel() {

    @JvmField
    protected var TAG = this.javaClass.simpleName

    private val _onLoadingStateFlow = MutableStateFlow(false)
    val onLoadingStateFlow = _onLoadingStateFlow.asStateFlow()

    protected fun showLoading() {
        _onLoadingStateFlow.value = true
    }

    protected fun hideLoading() {
        _onLoadingStateFlow.value = false
    }

    protected fun <T> Flow<T>.flowWithViewModelScope(
        isShowLoading: Boolean = true,
        isHandleError: Boolean = true
    ): Flow<T> {
        return this
            .flowOn(Dispatchers.IO)
            .onStart { if (isShowLoading) showLoading() }
            .catch {
                Log.e(TAG, "flowWithViewModelScope: Exception $it")
                if (isHandleError && handleError(it)) return@catch
                throw it
            }.onCompletion {
                if (isShowLoading) hideLoading()
            }
    }

    private fun handleError(throwable: Throwable): Boolean {
        //not yet implemented
        return true
    }
}

