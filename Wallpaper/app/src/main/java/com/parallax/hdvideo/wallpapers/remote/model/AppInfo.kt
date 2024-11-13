package com.parallax.hdvideo.wallpapers.remote.model

class AppInfo {
    var appVersion: String = ""
    var preferencesName: String = ""
    var notifyChannelName: String = ""
    var appConfigId: String = ""
    var wallDefaultDataFileName: String = ""
    var iconStatus = IconStatus()

    companion object {
        open val shared = AppInfo()
    }

}