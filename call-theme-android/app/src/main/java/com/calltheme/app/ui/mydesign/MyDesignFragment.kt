package com.calltheme.app.ui.mydesign

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.base.BaseFragment
import com.screentheme.app.R
import com.screentheme.app.databinding.FragmentMyDesignBinding
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.extensions.FragmentDirections
import com.screentheme.app.utils.helpers.ThemeManager
import org.koin.android.ext.android.inject

class MyDesignFragment : BaseFragment() {

    val myDesignViewModel: MyDesignViewModel by inject()

    lateinit var binding: FragmentMyDesignBinding

    private lateinit var adapter: MyThemeAdapter
    private var myThemes = ArrayList<ThemeConfig>()

    override fun getViewBinding(): ViewBinding {
        binding = FragmentMyDesignBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {

        adapter = MyThemeAdapter(requireContext(), showDeleteButton = true)
        binding.myThemeRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.myThemeRecyclerView.adapter = adapter
        adapter.setCallBack {

            val args = Bundle()
            args.putParcelable("themeConfig", it)

            val action = FragmentDirections.action(
                args,
                R.id.action_navigation_home_to_navigation_set_call_theme
            )
            try {
                getNavController().navigate(action)
            } catch (exception: Exception) {

            }

        }

        adapter.setOnDeleteItem { themeConfig ->
            if (themeConfig.id == themeManager.currentThemeId) {
                themeManager.currentThemeId = ThemeManager.DEFAULT_THEME_ID
            }
            themeManager.removeTheme(themeId = themeConfig.id)
        }

        binding.apply {
            goProButton.visibility = View.GONE
//            binding.goProButton.visibility =
//                if (BillingClientHelper.getInstance(requireActivity()).isPurchased) View.GONE else View.VISIBLE
            goProButton.setOnClickListener {
                try {
                    getNavController().navigate(R.id.action_navigation_home_to_navigation_subscription)
                } catch (exception: Exception) {

                }
            }
        }
    }

    override fun registerObservers() {

        myDesignViewModel.observeData( this)

        myDesignViewModel.myThemes.observe(this) {
            myThemes = it
            adapter.updateItems(myThemes)
        }
    }

}