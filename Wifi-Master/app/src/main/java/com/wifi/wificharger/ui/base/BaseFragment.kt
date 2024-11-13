package com.wifi.wificharger.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.wifi.wificharger.data.remote.RemoteConfig
import com.wifi.wificharger.ui.main.MainActivity

abstract class BaseFragment<VB: ViewBinding, ViewModel : BaseViewModel>(
    private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
) : Fragment() {

    private var _viewBinding: VB? = null
    abstract val viewModel: ViewModel
    protected val viewBinding get() = _viewBinding!!
    protected val remoteConfig: RemoteConfig = RemoteConfig()

    private val navigateAction: (bundle: Bundle?, actionId: Int) -> NavDirections = { bundle, actionId ->
        object : NavDirections {
            override val actionId: Int
                get() = actionId
            override val arguments: Bundle
                get() = bundle ?: bundleOf()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback {
            navigateUp()
        }
    }

    protected fun navigate(bundle: Bundle? = null, actionId: Int) {
        try {
            if (findNavController().currentDestination?.getAction(actionId) != null) {
                val action = navigateAction.invoke(bundle, actionId)
                findNavController().navigate(action)
            }
        } catch (e: Exception) {

        }
    }

    protected open fun navigateUp() {
        findNavController().navigateUp()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = inflate(inflater, container, false)
        return requireNotNull(_viewBinding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loadAds()
        observeData()
        onBindingAds()
    }

    open fun onBindingAds() { }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    fun getNavController(): NavController {
        val mainActivity = activity as MainActivity
        return mainActivity.navController
    }

    protected abstract fun initView()

    abstract fun observeData()

    abstract fun loadAds()
}