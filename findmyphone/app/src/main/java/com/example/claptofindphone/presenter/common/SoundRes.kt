package com.example.claptofindphone.presenter.common

import com.example.claptofindphone.presenter.select.SelectFragment
import com.example.claptofindphone.R

object SoundRes {
    fun getSound(soundType: Int): Int {
        return when (soundType) {
            // replace your sound here
            SelectFragment.SoundCategory.Police.value -> {
                R.raw.sound_type_police
            }

            SelectFragment.SoundCategory.Hello.value -> {
                R.raw.hello
            }

            SelectFragment.SoundCategory.Doorbell.value -> {
                R.raw.doorbell
            }

            SelectFragment.SoundCategory.Laughing.value -> {
                R.raw.sound_type_laughing
            }

            SelectFragment.SoundCategory.Alarm.value -> {
                R.raw.alarm
            }

            SelectFragment.SoundCategory.Harp.value -> {
                R.raw.harp
            }

            SelectFragment.SoundCategory.Piano.value -> {
                R.raw.piano
            }

            SelectFragment.SoundCategory.Rooster.value -> {
                R.raw.rooster
            }

            SelectFragment.SoundCategory.Sneeze.value -> {
                R.raw.sneeze
            }

            SelectFragment.SoundCategory.Train.value -> {
                R.raw.train
            }

            SelectFragment.SoundCategory.WindChimes.value -> {
                R.raw.chimes
            }

            SelectFragment.SoundCategory.Whistle.value -> {
                R.raw.whistle
            }

            else ->{
                R.raw.sound_type_police
            }
        }
    }
}