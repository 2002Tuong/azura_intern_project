package com.parallax.hdvideo.wallpapers.ui.base.activity;

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY
import android.view.inputmethod.InputMethodManager.SHOW_FORCED
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.internal.Primitives
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.FailedResponse
import com.parallax.hdvideo.wallpapers.extension.setStatusBar
import com.parallax.hdvideo.wallpapers.ui.base.NavigateCallback
import com.parallax.hdvideo.wallpapers.ui.base.NavigationController
import com.parallax.hdvideo.wallpapers.ui.base.NavigationControllerImp
import com.parallax.hdvideo.wallpapers.ui.base.dialog.ProgressDialogFragment
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.ui.dialog.ConfirmDialogFragment
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus
import com.livewall.girl.wallpapers.extension.openNetworkSettings
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.KClass

abstract class BaseActivity : AppCompatActivity(), NavigateCallback {

    abstract val resLayoutId: Int
    protected lateinit var rootView: View
    private val handler = Handler(Looper.getMainLooper())
    private var dialogProgress: ProgressDialogFragment? = null
    private var loading = false
    private lateinit var navigationControllerImp: NavigationControllerImp
    val navigationController: NavigationController get() = navigationControllerImp
    protected var isAppInFore = false
    private var shouldShownNetworkDialog = false
    private val networkTag = "Network_TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        navigationControllerImp = NavigationControllerImp(supportFragmentManager)
        navigationControllerImp.navigateCallback = this
        super.onCreate(savedInstanceState)
        setStatusBar(true)
        rootView = layoutInflater.inflate(resLayoutId, null)
        setContentView(rootView)
        init(rootView)
        RxBus.subscribe(name = networkTag, clazz = FailedResponse::class) { error ->
            if (isFinishing || isDestroyed || !isAppInFore) return@subscribe
            val code = error.code
            if (code == -1) {
                val message = (error.cause?.message ?: "").toLowerCase(Locale.ENGLISH) // !message.contains("closed") && !message.contains("canceled")
                if  (!error.online) {
                    showNetworkDialog(getString(R.string.message_toast_connect_network))
                }
                Logger.d("From RxBus BaseActivity", error.url, message)
            } else {
                if (!isFinishing)
                    handleResponse(code, error.cause)
            }
        }
    }

    open fun init(view: View) {

    }

    open fun <T: Fragment>findFragment(clazz: KClass<T>, tag: String? = null) : T? {
        try {
            val clazzName = clazz.java.name
            val tag1 = tag ?: clazzName
            val fragment = supportFragmentManager.findFragmentByTag(tag1)
            if (fragment != null) {
                return Primitives.wrap(clazz.java).cast(fragment)
            }
        } catch (e: Exception) {

        }
        return null
    }

    fun setStatusBar(show: Boolean, isLight: Boolean = false) {
        window.setStatusBar(show, isLight)
    }

    fun setStatusBarColor(color: Int) {
        window.statusBarColor = color
    }

    var isTransparentStatusBar: Boolean
         set(value) {
             window.statusBarColor = if (value) Color.TRANSPARENT else ContextCompat.getColor(this, R.color.colorSecondary)
         }
        get() = window.statusBarColor == Color.TRANSPARENT


    open fun setTransparentNavigationBar(on: Boolean) {
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, on)
        window.navigationBarColor = if (on) Color.TRANSPARENT else Color.BLACK
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)
    }

    open fun setWindowFlag(bits: Int, on: Boolean) {
        val windowParams = window.attributes
        if (on) {
            windowParams.flags = windowParams.flags or bits
        } else {
            windowParams.flags = windowParams.flags and bits.inv()
        }
        window.attributes = windowParams
    }

    fun showNetworkDialog(message: String? = null) {
        return
        if (isFinishing || isDestroyed) return
        if (shouldShownNetworkDialog) return
        shouldShownNetworkDialog = true
        ConfirmDialogFragment.show(supportFragmentManager,
            message = message ?: getString(R.string.message_toast_connect_network),
            tag = networkTag,
            callbackNo = {
                shouldShownNetworkDialog = false
                if (!isFinishing) {
                    didClickConfirmNetwork()
                }
            },
            callbackYes = {
                shouldShownNetworkDialog = false
                if (!isFinishing)
                    if (!openNetworkSettings())
                        showToast("Cannot open settings")
            })
    }
    
    @Synchronized
    fun dismissNetworkDialog() {
        try {
            findFragment<ConfirmDialogFragment>(networkTag)?.dismissAllowingStateLoss()
        }catch (e: Exception) {
            
        }
    }

//    override fun finish() {
//        if (isValid)
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//        super.finish()
//    }

    override fun onResume() {
        super.onResume()
        isAppInFore = true
    }

    override fun onPause() {
        super.onPause()
        isAppInFore = false
    }

    override fun onBackPressed() {
        val tag = navigationControllerImp.peek
        if (tag != null) {
            val lastFragment = navigationControllerImp.findFragment(BaseFragment::class, tag = tag)
            if (lastFragment != null) {
                lastFragment.onBackPressed()
                return
            }
        }
        if (!navigationControllerImp.popFragment()) {
            val fg = currentFragment
            if (fg != null) {
                if (fg.onBackPressed()) {
                    return
                }
            }
        }
        if (shouldOverrideBackPressed()) {
            super.onBackPressed()
        }
    }

    var currentFragment : BaseFragment? = null

    override fun prepareToPushFragment(fragment: Fragment) {
        topFragment?.let {
            it.childFragmentManager.fragments.forEach { child ->
                (child as? BaseFragment)?.prepareToPushFragment(fragment)
            }
            it.prepareToPushFragment(fragment)
        }
    }

    val topFragment: BaseFragment? get() {
        var fragment = (navigationController.topFragment as? BaseFragment) ?: currentFragment
        while (fragment != null) {
            val fg = fragment.navigationController?.topFragment as? BaseFragment
            if (fg == null) return fragment
            else fragment = fg
        }
        return fragment
    }

    override fun didPushFragment(fragment: Fragment) {
        topFragment?.let {
            it.childFragmentManager.fragments.forEach { child ->
                (child as? BaseFragment)?.didPushFragment(fragment)
            }
            it.didPushFragment(fragment)
        }
    }

    override fun didRemoveFragment(fragment: Fragment) {
        topFragment?.let {
            it.childFragmentManager.fragments.forEach { child ->
                (child as? BaseFragment)?.didRemoveFragment(fragment)
            }
            it.didRemoveFragment(fragment)
        }
    }
    
    inline fun <reified T: Fragment>findFragment(tag: String? = null) : T? {
        return if (tag != null)
         supportFragmentManager.findFragmentByTag(tag) as? T
        else supportFragmentManager.fragments.lastOrNull { it as? T != null } as? T
    }

    open fun shouldOverrideBackPressed() : Boolean {
        return true
    }

    override fun onDestroy() {
        RxBus.unregister(name = networkTag)
        dialogProgress = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    fun showToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    fun showToast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
    }

    open fun handleResponse(code: Int, cause: Any?) {
        Logger.d( "Error status = " + code + " ; cause = " + cause?.toString())
    }

    open fun didClickConfirmNetwork() {

    }

    //region Progress bar
    @Synchronized
    fun showLoading(touchOutside: Boolean, canGoBack: Boolean) {
        synchronized("ProgressDialog") {
            if (isFinishing || loading || isDestroyed) {
                return
            }
            loading = true
            try {
                val dialog = dialogProgress ?: ProgressDialogFragment()
                dialogProgress = dialog
                if (!dialog.isAdded) {
                    dialog.canceledWhenTouchOutside = touchOutside
                    dialog.isGoBack = canGoBack
                    dialog.show(supportFragmentManager, ProgressDialogFragment::class.java.name)
                }
            }catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    fun showLoading() {
        this.showLoading(touchOutside = false, canGoBack = true)
    }

    fun dismissLoading() {
        if (isFinishing || !loading || isDestroyed) {
            return
        }
        loading = false

        try {
            dialogProgress?.dismissAllowingStateLoss()
            dialogProgress = null
        } catch (e: Exception) {
            post {
                try {
                    dialogProgress?.dismissAllowingStateLoss()
                    dialogProgress = null
                } catch (er: Exception){
                    Logger.d("dismissLoading", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
    }

    open fun onBackPressedLoading() {
        topFragment?.onBackPressedLoading()
    }

    fun hiddenKeyboard() {
//        val view = focusedViewOnActionDown ?: return
        val currentFocus = this.currentFocus as? EditText ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.toggleSoftInput(HIDE_IMPLICIT_ONLY, 0)
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
//        currentFocus.isFocusableInTouchMode = true
//        currentFocus.isFocusable = false
//        fixInputMethod()
    }

//    fun showKeyboard() {
////        val currentFocus = this.currentFocus as? EditText ?: return
//        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
////        inputMethodManager.showSoftInput(currentFocus, 0)
//        inputMethodManager.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)
////        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
//    }

    fun showKeyboard(view: View? = null) {
//        val currentFocus = this.currentFocus as? EditText ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = view ?: this.currentFocus as? EditText
        if (v != null) {
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        } else {
            imm.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)
        }
//        inputMethodManager.toggleSoftInput(SHOW_FORCED, HIDE_IMPLICIT_ONLY)
//        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    open fun fixInputMethod() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val declaredFields: Array<Field> = imm.javaClass.declaredFields
//        for (declaredField in declaredFields) {
//            try {
//                if (!declaredField.isAccessible) {
//                    declaredField.isAccessible = true
//                }
//                val view = declaredField.get(inputMethodManager) as? View
//                view?.also {
//                    if (it.context == this) {
//                        declaredField.set(inputMethodManager, null)
//                    }
//                }
//            } catch (th: IOException) {
//                th.printStackTrace()
//            }
//        }
    }

    //endregion

    fun post(runnable: Runnable) {
        if (isFinishing || isDestroyed) return
        handler.post(runnable)
    }

    fun post(runnable: (() -> Unit)) {
        if (isFinishing || isDestroyed) return
        handler.post(runnable)
    }

    fun postDelayed(runnable: Runnable, delayMillis: Long) {
        if (isFinishing || isDestroyed) return
        handler.postDelayed(runnable, delayMillis)
    }

    fun removeCallbacks(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }

    companion object {
        const val TAG_PUSH_ANIMATE_FRAGMENT = "TAG_PUSH_ANIMATE_FRAGMENT"
        const val TAG_NAME_FRAGMENT = "TAG_CLASS_NAME_FRAGMENT"

    }
}
