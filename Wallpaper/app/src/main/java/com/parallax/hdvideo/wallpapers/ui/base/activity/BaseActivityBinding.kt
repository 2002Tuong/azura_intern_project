
package com.parallax.hdvideo.wallpapers.ui.base.activity

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import java.lang.reflect.ParameterizedType


abstract class BaseActivityBinding<T: ViewDataBinding, V: BaseViewModel> : BaseActivity() {

    open lateinit var dataBinding: T
    open lateinit var viewModel: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ViewModel", "callFirst1")
        try {
            dataBinding = DataBindingUtil.bind(rootView)!!
            dataBinding.lifecycleOwner = this
            @Suppress("UNCHECKED_CAST")
            val clazz: Class<V> = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<V>
            Log.d("ViewModel", "callFirst2")
            viewModel = ViewModelProvider(this).get(clazz)
        } catch (e: Exception) {
            Log.d("ViewModel", "Message: ${e.message}")
            Log.d("ViewModel", "Message: ")
            e.printStackTrace()
            finish()
            return
        }
        viewModel.progressDialog.observe(this, Observer {
            if (it.status) showLoading(it.touchOutside, it.canGoBack)
             else dismissLoading()
        })
        viewModel.toastLiveData.observe(this,  Observer {
            if (it is Int) {
                showToast(it)
            } else {
                showToast(it.toString())
            }
        })
    }

    override fun onDestroy() {
        if (this::dataBinding.isInitialized)
            dataBinding.unbind()
        super.onDestroy()
    }

    protected val isInitialized get() = this::dataBinding.isInitialized && this::viewModel.isInitialized

}