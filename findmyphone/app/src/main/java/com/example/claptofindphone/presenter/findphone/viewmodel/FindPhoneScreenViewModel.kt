package com.example.claptofindphone.presenter.findphone.viewmodel

import androidx.lifecycle.ViewModel
import com.example.claptofindphone.R
import com.example.claptofindphone.presenter.select.SelectFragment
import com.example.claptofindphone.presenter.select.model.SoundModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FindPhoneScreenViewModel @Inject constructor() : ViewModel() {

    private var soundTypes = ArrayList<SoundModel>()

    fun setUpSoundTypeList(): ArrayList<SoundModel> {
        if (soundTypes.isNotEmpty()) {
            soundTypes.clear()
        }
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Police,
                R.drawable.icon_sound_police,
                R.string.sound_police
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Hello,
                R.drawable.icon_sound_hello,
                R.string.sound_hello
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Doorbell,
                R.drawable.icon_sound_doorbell,
                R.string.sound_doorbell
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Laughing,
                R.drawable.icon_sound_laughing,
                R.string.sound_laughing
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Alarm,
                R.drawable.icon_sound_alarm,
                R.string.sound_alarm
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Harp,
                R.drawable.icon_sound_harp,
                R.string.sound_harp
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Piano,
                R.drawable.icon_sound_piano,
                R.string.sound_piano

            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Rooster,
                R.drawable.icon_sound_rooster,
                R.string.sound_rooster
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Sneeze,
                R.drawable.icon_sound_sneeze,
                R.string.sound_sneeze
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Train,
                R.drawable.icon_sound_train,
                R.string.sound_train
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.WindChimes,
                R.drawable.icon_sound_wind_chimes,
                R.string.sound_wind_chimes
            )
        )
        soundTypes.add(
            SoundModel(
                SelectFragment.SoundCategory.Whistle,
                R.drawable.icon_sound_whistle,
                R.string.sound_whistle
            )
        )
        return soundTypes
    }
}