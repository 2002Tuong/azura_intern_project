package com.calltheme.app.ui.diytheme

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.activity.BaseActivity
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.pickavatar.PickAllAvatarsFragment
import com.calltheme.app.utils.AdsUtils
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentDiyThemeBinding
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.models.CallIconModel
import com.screentheme.app.models.ThemeConfig
import com.screentheme.app.utils.extensions.FragmentDirections
import com.screentheme.app.utils.extensions.getShowInterCustomizeCount
import com.screentheme.app.utils.extensions.increaseShowInterCustomizeCount
import com.screentheme.app.utils.helpers.SharePreferenceHelper
import com.screentheme.app.utils.helpers.ThemeManager
import org.koin.android.ext.android.inject

class DiyThemeFragment : BaseFragment() {

    private lateinit var binding: FragmentDiyThemeBinding

    private lateinit var callIconAdapter: CallIconAdapter

    private lateinit var avatarAdapter: AvatarAdapter

    private lateinit var backgroundAdapter: BackgroundAdapter

    private var isWaitingToShowInter = false


    override fun getViewBinding(): ViewBinding {
        binding = FragmentDiyThemeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        AdsUtils.loadInterCustomize(requireContext())
        binding.callIconRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        callIconAdapter = CallIconAdapter()
        callIconAdapter.setCallBack {
            val baseThemeConfig = themeManager
                .getThemeConfig(ThemeManager.DEFAULT_THEME_ID)
            if (baseThemeConfig != null) {
                val themeConfig = ThemeConfig(
                    background = baseThemeConfig.background,
                    avatar = baseThemeConfig.avatar,
                    acceptCallIcon = it.accept_call_icon,
                    declineCallIcon = it.decline_call_icon
                )

                val args = Bundle()
                args.putParcelable("themeConfig", themeConfig)
                args.putString("screen", "DiyThemeFragment")

                val action = FragmentDirections.action(
                    args,
                    R.id.action_navigation_home_to_navigation_set_call_theme
                )
                try {
                    myActivity?.let {
                        AdsUtils.forceShowInterCustomize(it) {
                            navigate(action)
                        }
                    }
                } catch (exception: IllegalArgumentException) {

                }
            }
        }

        binding.callIconRecyclerView.adapter = callIconAdapter

        binding.avatarRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        avatarAdapter = AvatarAdapter()
        avatarAdapter.setCallBack { position, avatar ->
            if (position == 0) {

                val mainActivity = activity as BaseActivity

                mainActivity.pickImageFromGallery { imageUri ->
                    Log.d("CustomTheme", "imageUri: ${imageUri}")
                    val baseThemeConfig = themeManager
                        .getThemeConfig(ThemeManager.DEFAULT_THEME_ID)
                    if (baseThemeConfig != null) {
                        val themeConfig = ThemeConfig(
                            background = baseThemeConfig.background,
                            avatar = imageUri.toString(),
                            acceptCallIcon = baseThemeConfig.acceptCallIcon,
                            declineCallIcon = baseThemeConfig.declineCallIcon
                        )

                        val args = Bundle()
                        args.putParcelable("themeConfig", themeConfig)
                        args.putString("screen", "DiyThemeFragment")

                        val action = FragmentDirections.action(
                            args,
                            R.id.action_navigation_home_to_navigation_set_call_theme
                        )
                        navigate(action)
                    }
                }
            } else {
                if (avatar == null) return@setCallBack
                val baseThemeConfig = themeManager
                    .getThemeConfig(ThemeManager.DEFAULT_THEME_ID)
                if (baseThemeConfig != null) {
                    val themeConfig = ThemeConfig(
                        background = baseThemeConfig.background,
                        avatar = avatar.avatar,
                        acceptCallIcon = baseThemeConfig.acceptCallIcon,
                        declineCallIcon = baseThemeConfig.declineCallIcon
                    )

                    val args = Bundle()
                    args.putParcelable("themeConfig", themeConfig)
                    args.putString("screen", "DiyThemeFragment")

                    val action = FragmentDirections.action(
                        args,
                        R.id.action_navigation_home_to_navigation_set_call_theme
                    )
                    try {
                        myActivity?.let {
                            AdsUtils.forceShowInterCustomize(it) {
                                navigate(action)
                            }
                        }
                    } catch (exception: IllegalArgumentException) {

                    }
                }
            }
        }
        binding.avatarRecyclerView.adapter = avatarAdapter

        backgroundAdapter = BackgroundAdapter()
        backgroundAdapter.setCallBack { position, background ->
            if (position == 0) {
                val mainActivity = activity as BaseActivity

                mainActivity.pickImageFromGallery { imageUri ->

                    if (imageUri == null) {
                        return@pickImageFromGallery
                    }

                    val baseThemeConfig = themeManager
                        .getThemeConfig(ThemeManager.DEFAULT_THEME_ID)

                    if (baseThemeConfig != null) {
                        val themeConfig = ThemeConfig(
                            background = imageUri.toString(),
                            avatar = baseThemeConfig.avatar,
                            acceptCallIcon = baseThemeConfig.acceptCallIcon,
                            declineCallIcon = baseThemeConfig.declineCallIcon
                        )

                        val args = Bundle()
                        args.putParcelable("themeConfig", themeConfig)
                        args.putString("screen", "DiyThemeFragment")

                        val action = FragmentDirections.action(
                            args,
                            R.id.action_navigation_home_to_navigation_set_call_theme
                        )
                        navigate(action)
                    }
                }
            } else {
                if (background == null) return@setCallBack
                val baseThemeConfig = themeManager
                    .getThemeConfig(ThemeManager.DEFAULT_THEME_ID)

                if (baseThemeConfig != null) {
                    val themeConfig = ThemeConfig(
                        background = background.background,
                        avatar = baseThemeConfig.avatar,
                        acceptCallIcon = baseThemeConfig.acceptCallIcon,
                        declineCallIcon = baseThemeConfig.declineCallIcon
                    )

                    val args = Bundle()
                    args.putParcelable("themeConfig", themeConfig)
                    args.putString("screen", "DiyThemeFragment")

                    val action = FragmentDirections.action(
                        args,
                        R.id.action_navigation_home_to_navigation_set_call_theme
                    )
                    try {
                        myActivity?.let {
                            AdsUtils.forceShowInterCustomize(it) {
                                navigate(action)
                            }
                        }
                    } catch (exception: IllegalArgumentException) {

                    }
                }
            }
        }
        binding.backgroundRecyclerView.isNestedScrollingEnabled = false
        binding.backgroundRecyclerView.setHasFixedSize(false)
        binding.backgroundRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.backgroundRecyclerView.adapter = backgroundAdapter

        binding.addAvatarButton.setOnClickListener {

            val mainActivity = activity as BaseActivity
            mainActivity.pickImageFromGallery { imageUri ->
                if (imageUri == null) {
                    return@pickImageFromGallery
                }

                val baseThemeConfig = themeManager
                    .getThemeConfig(ThemeManager.DEFAULT_THEME_ID)

                if (baseThemeConfig != null) {
                    val themeConfig = ThemeConfig(
                        background = baseThemeConfig.background,
                        avatar = imageUri.toString(),
                        acceptCallIcon = baseThemeConfig.acceptCallIcon,
                        declineCallIcon = baseThemeConfig.declineCallIcon
                    )

                    val args = Bundle()
                    args.putParcelable("themeConfig", themeConfig)

                    val action = FragmentDirections.action(
                        args,
                        R.id.action_navigation_home_to_navigation_set_call_theme
                    )
                    navigate(action)
                }
            }
        }

        binding.avatarViewAll.setOnClickListener {
            val args = Bundle()
            args.putInt("type", PickAllAvatarsFragment.KEY_AVATARS)

            val action = FragmentDirections.action(
                args,
                R.id.action_navigation_home_to_navigation_pick_all_avatars
            )
            navigate(action)
        }

        binding.backgroundViewAll.setOnClickListener {
            val args = Bundle()
            args.putInt("type", PickAllAvatarsFragment.KEY_BACKGROUNDS)

            val action = FragmentDirections.action(
                args,
                R.id.action_navigation_home_to_navigation_pick_all_avatars
            )
            navigate(action)
        }

        binding.callIconViewAll.setOnClickListener {
            val args = Bundle()
            args.putInt("type", PickAllAvatarsFragment.KEY_CALL_ICONS)

            val action = FragmentDirections.action(
                args,
                R.id.action_navigation_home_to_navigation_pick_all_avatars
            )
            navigate(action)
        }
        binding.goProButton.visibility = View.GONE

//        binding.goProButton.visibility =
//            if (BillingClientHelper.getInstance(requireActivity()).isPurchased) View.GONE else View.VISIBLE
        binding.goProButton.setOnClickListener {
            try {
                getNavController().navigate(R.id.action_navigation_home_to_navigation_subscription)
            } catch (ex: Exception) {

            }
        }
    }

    private fun navigate(action: NavDirections) {
        try {
            isWaitingToShowInter = action.actionId in listOf(
                R.id.action_navigation_home_to_navigation_pick_all_avatars,
                R.id.action_navigation_home_to_navigation_set_call_theme
            )
            Log.d("qvk23", "isWaiting: $isWaitingToShowInter")
            getNavController().navigate(action)
        } catch (exception: IllegalArgumentException) {

        }
    }
    override fun onResume() {
        super.onResume()
//        if (isWaitingToShowInter) {
//            context?.let {
//                SharePreferenceHelper.increaseShowInterCustomizeCount(it)
//                if (SharePreferenceHelper.getShowInterCustomizeCount(it) >= 2) {
//                    myActivity?.let { it1 ->
//                        AdsUtils.forceShowInterCustomize(it1, onNextAction = {
//                            isWaitingToShowInter = false
//                        })
//                    }
//                }
//            }
//        }
    }

    override fun registerObservers() {

        val avatars = themeManager.getResources(AppRemoteConfig.callThemeConfigs(), AvatarModel::class.java) as ArrayList<AvatarModel>
        avatarAdapter.updateItems(avatars)

        val callIcons =
            themeManager.getResources(AppRemoteConfig.callThemeConfigs(), CallIconModel::class.java) as ArrayList<CallIconModel>
        callIconAdapter.updateItems(callIcons)

        val backgrounds =
            themeManager.getResources(AppRemoteConfig.callThemeConfigs(), BackgroundModel::class.java) as ArrayList<BackgroundModel>
        backgroundAdapter.updateItems(backgrounds)
    }

}