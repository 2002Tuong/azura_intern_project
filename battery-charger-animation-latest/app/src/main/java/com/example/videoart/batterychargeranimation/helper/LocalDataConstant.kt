package com.example.videoart.batterychargeranimation.helper

import com.example.videoart.batterychargeranimation.model.RemoteTheme
import com.example.videoart.batterychargeranimation.model.Theme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val LOCAL_THEMES = """
[
  {
    "id": "0117",
    "type": "1",
    "font_family": "https://charging-battery.ap-south-1.linodeobjects.com/Fonts/NewsportDemoRegular.ttf",
    "text_size": 70,
    "text_color": "#F2F0EE",
    "category_id": "9",
    "free": false,
    "thumb_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/38/38 - Thumb.png",
    "sound_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/38/Selection.mp3",
    "animatation_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/38/38 - APNG.webp"
  },
  {
    "id": "0118",
    "type": "1",
    "font_family": "https://charging-battery.ap-south-1.linodeobjects.com/Fonts/Race Sport.ttf",
    "text_size": 70,
    "text_color": "#FAD446",
    "category_id": "9",
    "free": true,
    "thumb_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/39/39 - Thumb.png",
    "sound_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/39/HitWood SME02_59.2.wav",
    "animatation_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/39/39 - APNG.webp"
  },
  {
    "id": "0119",
    "type": "1",
    "font_family": "https://charging-battery.ap-south-1.linodeobjects.com/Fonts/Vogue.ttf",
    "text_size": 70,
    "text_color": "#87E3F6",
    "category_id": "9",
    "free": false,
    "thumb_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/40/40 - Thumb.png",
    "sound_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/40/Modern UI Sound.wav",
    "animatation_url": "https://charging-battery.ap-south-1.linodeobjects.com/Sport/40/40 - APNG.webp"
  }
]
"""

fun convertJsonToRemoteThemes(jsonString: String): List<RemoteTheme> {
    val gson = Gson()
    val listType = object : TypeToken<List<RemoteTheme>>() {}.type
    return gson.fromJson(jsonString, listType)
}

val localScreenRemoteThemes: List<RemoteTheme> = convertJsonToRemoteThemes(LOCAL_THEMES)
