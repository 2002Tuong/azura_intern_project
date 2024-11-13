package com.slideshowmaker.slideshow.ui.select_music

import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein

class SelectMusicActivity : BaseActivity(), KodeinAware {


    override fun getContentResId(): Int = R.layout.activity_select_music_screen

    override val kodein by closestKodein()

    override fun initViews() {
        if (intent.getStringExtra("music_source") == "online") {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.rootLayout,
                    SelectMusicFragment.newInstance(SelectMusicFragment.MusicType.ONLINE)
                )
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.rootLayout,
                    SelectMusicFragment.newInstance(SelectMusicFragment.MusicType.LOCAL)
                )
                .commit()
        }


        setRightButton(R.drawable.icon_search_white_24dp) {
            showSearchInput()
        }

        setSearchInputListener {
            onSearch(it)
        }

    }

    override fun initActions() {

    }

    private fun onSearch(query: String) {


    }


    override fun onBackPressed() {
        if (searchingMode) {
            hideSearchInput()
        } else {
            super.onBackPressed()

        }

    }

    override fun screenTitle(): String = getString(R.string.select_music)

    companion object {
        const val SELECT_MUSIC_REQUEST_CODE = 1001
        const val MUSIC_RETURN_DATA_KEY = "SelectMusicActivity.MUSIC_RETURN_DATA_KEY"
    }

}
