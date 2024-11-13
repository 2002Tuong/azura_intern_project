package com.example.claptofindphone.presenter.splash

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.data.local.PreferenceSupplier.Companion.LANGUAGE_UNIQUE_CODE
import com.example.claptofindphone.databinding.ActivitySplashScreenBinding
import com.example.claptofindphone.presenter.MainActivity
import com.example.claptofindphone.presenter.language.LanguageActivity
import com.example.claptofindphone.utils.AdsHelper
import com.example.claptofindphone.utils.LanguageSupporter
import com.example.claptofindphone.utils.extensions.launchAndRepeatOnLifecycleStarted
import com.ironsource.mediationsdk.IronSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var layoutBinding: ActivitySplashScreenBinding

    @Inject
    lateinit var languageSupporter: LanguageSupporter
    @Inject
    lateinit var adsHelper: AdsHelper
    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier

    private val viewModel: SplashScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!preferenceSupplier.firstOpenComplete) {
            preferenceSupplier.deleteData(LANGUAGE_UNIQUE_CODE)
        }
        languageSupporter.loadLocale(this)
        layoutBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
        viewModel.loadData(this)
        launchAndRepeatOnLifecycleStarted {
            viewModel.navigateToMainScreenState.collectLatest {
                if(it) {
                    navigateTo()
                }
            }
        }

        launchAndRepeatOnLifecycleStarted {
            viewModel.loadingState.collect {
                layoutBinding.loadingProgress.isVisible = it
            }
        }

    }

    public override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }
    private fun navigateTo() {
        if (!preferenceSupplier.firstOpenComplete) {
            launchLanguage()
        } else {
            launchHome()
        }
    }

    private fun launchLanguage() {
        LanguageActivity.start(this, false)
    }

    private fun launchHome() {
        Intent(this, MainActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkShowInterSplashWhenFail(this)
        IronSource.onResume(this)
    }
}