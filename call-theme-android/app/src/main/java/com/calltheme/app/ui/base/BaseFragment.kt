package com.calltheme.app.ui.base

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.activity.BaseActivity
import com.calltheme.app.ui.activity.MainActivity
import com.calltheme.app.ui.dialog.DialogLoadingFragment
import com.calltheme.app.ui.dialog.ExitAppPopup
import com.calltheme.app.ui.dialog.PermissionsPopup
import com.calltheme.app.ui.dialog.RatingAppPopup
import com.calltheme.app.ui.picklanguage.PickLanguageViewModel
import com.screentheme.app.utils.helpers.ThemeManager
import org.koin.android.ext.android.inject

abstract class BaseFragment : Fragment(), LifecycleObserver {
    private var rootView: View? = null
    private var needLoadData = false

    protected var myActivity: AppCompatActivity? = null
    protected var timer: CountDownTimer? = null

    val permissionsPopup = PermissionsPopup()
    val exitAppPopup = ExitAppPopup()
    val ratingAppPopup = RatingAppPopup()
    val loadingDialog = DialogLoadingFragment()

    val themeManager: ThemeManager by inject()
    protected lateinit var pickLanguageViewModel: PickLanguageViewModel

    abstract fun getViewBinding(): ViewBinding

    abstract fun onViewCreated()

    abstract fun registerObservers()

    open fun initializeViewModels() {
        pickLanguageViewModel = requireActivity().run {
            ViewModelProvider(this).get(PickLanguageViewModel::class.java)
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreated() {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(this)

        initializeViewModels()
        //remoteConfig.premiumConfigs
        if (context is BaseActivity) {
            myActivity = context
        }
    }

    override fun onDetach() {
        activity?.lifecycle?.removeObserver(this)
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (rootView == null) {
            rootView = getViewBinding().root
            needLoadData = true
        } else {
            needLoadData = false
        }
        return rootView as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (needLoadData) {
            onViewCreated()
            registerObservers()
        }
        needLoadData = true
    }

    fun getNavController(): NavController {
        val mainActivity = activity as MainActivity
        return mainActivity.navController
    }


}