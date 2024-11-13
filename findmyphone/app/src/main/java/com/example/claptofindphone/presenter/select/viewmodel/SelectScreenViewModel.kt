package com.example.claptofindphone.presenter.select.viewmodel

import com.example.claptofindphone.presenter.base.BaseViewModel
import com.example.claptofindphone.presenter.select.model.SoundModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SelectScreenViewModel @Inject constructor() : BaseViewModel() {
    private val _soundModel = MutableStateFlow<SoundModel?>(null)
    val soundModel = _soundModel.asStateFlow()
    fun initData(soundModel: SoundModel) {
        _soundModel.value = soundModel
    }
}