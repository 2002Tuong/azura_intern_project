package com.calltheme.app.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig

class SettingPreferencesFragment : PreferenceFragmentCompat() {

    private var mCallback: ((preference: Preference) -> Unit)? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey)
        val cmpPreference = findPreference<Preference>("cmp_policy") ?: return
        if (AppRemoteConfig.cmpRequire) {
//            preferenceScreen.addPreference()
        } else {
            preferenceScreen.removePreference(cmpPreference)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        mCallback?.invoke(preference)
        return true
    }

    fun setOnPreferenceClickListener(callback: (preference: Preference) -> Unit) {
        this.mCallback = callback
    }

}