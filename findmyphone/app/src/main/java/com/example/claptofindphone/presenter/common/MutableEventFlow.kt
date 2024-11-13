package com.example.claptofindphone.presenter.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

class MutableEventFlow<T : Any> {
    private val _stateFlow = MutableStateFlow<T?>(null)

    fun asEventFlow() = _stateFlow.asStateFlow().filterNotNull()
        .onEach {
            _stateFlow.value = null
        }

    var value: T? = null
        set(value) {
            field = value
            _stateFlow.value = value
        }

    fun emit(value: T) {
        this.value = value
    }
}