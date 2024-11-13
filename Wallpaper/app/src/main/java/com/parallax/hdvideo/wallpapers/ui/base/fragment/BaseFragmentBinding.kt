package com.parallax.hdvideo.wallpapers.ui.base.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.parallax.hdvideo.wallpapers.extension.dismissLoading
import com.parallax.hdvideo.wallpapers.extension.popFragment
import com.parallax.hdvideo.wallpapers.extension.showLoading
import com.parallax.hdvideo.wallpapers.services.log.EventData
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.ProgressDialogFragment
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.utils.Logger
import java.lang.reflect.ParameterizedType

abstract class BaseFragmentBinding<T: ViewDataBinding, V: BaseViewModel>: BaseFragment() {

    open lateinit var dataBinding: T
    open lateinit var viewModel: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(javaClass.name, "onCreate()...")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            dataBinding = DataBindingUtil.bind(view)!!
            dataBinding.lifecycleOwner = this
            @Suppress("UNCHECKED_CAST")
            val clazz: Class<V> = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<V>
            viewModel = ViewModelProvider(this).get(clazz)
        } catch (e: Exception) {
            popFragment()
            TrackingSupport.recordFailConstructViewModel(EventData.FailConstructViewModel, "fragment")
            return
        }
        super.onViewCreated(view, savedInstanceState)
        observeLiveData(viewModel)
    }

    protected fun observeLiveData(viewModel: BaseViewModel) {
        viewModel.toastLiveData.observe(viewLifecycleOwner,  Observer {
            if (it is Int) {
                showToast(it)
            } else {
                showToast(it.toString())
            }
        })
        viewModel.progressDialog.observe(viewLifecycleOwner, Observer {
            if(handleLoading(it)) {
                if (it.status) showLoading(it.touchOutside, it.canGoBack)
                else dismissLoading()
            }
            Logger.d(it)
        })
    }

    open fun handleLoading(config: ProgressDialogFragment.Configure) : Boolean {
        return true
    }

    protected val isInitialized get() = this::viewModel.isInitialized && this::dataBinding.isInitialized

}