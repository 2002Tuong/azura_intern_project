package com.parallax.hdvideo.wallpapers.ui.base.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.extension.findFragment
import com.parallax.hdvideo.wallpapers.extension.popFragment
import com.parallax.hdvideo.wallpapers.ui.base.NavigateCallback
import com.parallax.hdvideo.wallpapers.ui.base.NavigationController
import com.parallax.hdvideo.wallpapers.ui.base.NavigationController.Companion.PARENT_FRAGMENT_NAME_TAG
import com.parallax.hdvideo.wallpapers.ui.base.NavigationControllerImp
import com.parallax.hdvideo.wallpapers.ui.base.activity.BaseActivity
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import kotlin.reflect.KClass

abstract class BaseFragment : Fragment(), NavigateCallback {

    private val handler = Handler()
    protected var delayedAmount: Long = 0
    private var rootView: View? = null
    companion object {
        // preview
        const val PREVIEW_CODE = 100
        // download
        const val DOWNLOAD_CODE = 101
        // set wallpapers
        const val SET_WALL_CODE = 102
        //Local Storage
        const val PERMISSION_STORAGE_CODE = 104
        // share
        const val SHARE_CODE = 105
    }

    abstract val resLayoutId: Int

    private lateinit var navigationControllerImp: NavigationControllerImp
    val navigationController: NavigationController? get() = if (this::navigationControllerImp.isInitialized) navigationControllerImp else null
    var dataLoaded = false

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationControllerImp = NavigationControllerImp(childFragmentManager)
        navigationControllerImp.navigateCallback = this

        Log.d(javaClass.name, "onCreate()...")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(resLayoutId, null)
        rootView = layout
        layout.isClickable = true
        layout.isFocusable = true
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (view != null && menuVisible && !dataLoaded) {
            dataLoaded = true
            postDelayed({
                viewIsVisible()
            } , 200)
        }
    }

    open fun viewIsVisible() {
    }

    fun showToast(str: String) {
        val context = activity ?: return
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    open fun showToast(@StringRes id: Int) {
        val context = activity ?: return
        Toast.makeText(context, id, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
        Log.d(javaClass.name, "onDestroy()...")
    }

    fun postDelayed(run: Runnable, delayMillis: Long ) {
        handler.postDelayed(run, delayMillis)
    }

    fun post(run : ()-> Unit) {
        handler.post(run)
    }

    fun postDelayed(run : ()-> Unit, delayMillis: Long) {
        handler.postDelayed(run, delayMillis)
    }

    fun removeAllCallbacks() {
        handler.removeCallbacksAndMessages(null)
    }
    fun removeCallbacks(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }

    override fun prepareToPushFragment(fragment: Fragment) {
        // to do implement
    }

    override fun didPushFragment(fragment: Fragment) {

    }

    override fun didRemoveFragment(fragment: Fragment) {

    }

    open fun refreshDataIfNeeded() {
        // to do implement
    }

    open fun onBackPressedLoading() {
        //Add if needed
    }

    protected open fun init(view: View) {
        //Add if needed
    }

    fun setStatusBar(show: Boolean, isLight: Boolean = false) {
        (activity as? MainActivity)?.setStatusBar(show, isLight)
    }

    /**
     *  Nếu trả về true là pop fragment và ngược lại là false
     */
    open fun onBackPressed() : Boolean {
        if (popChildFragment()) {
           return true
        }
        return popFragment()
    }

    fun openNetworkSettings() : Boolean {
        return if (!NetworkUtils.isNetworkConnected()) {
            (activity as? BaseActivity)?.showNetworkDialog()
            true
        } else false
    }

    fun hiddenKeyboard() {
        (activity as? BaseActivity)?.hiddenKeyboard()
    }

    fun showKeyboard(view: View? = null) {
        (activity as? BaseActivity)?.showKeyboard(view)
    }

    fun popChildFragment() : Boolean {
        if (navigationController?.popFragment() == true) {
            return true
        }
        val baseFragment = parentFragment as? BaseFragment ?: return false
        return baseFragment.navigationController?.popFragment(this, animate = false) ?: false
    }

    fun pushChildFragment(fragment: Fragment, bundle: Bundle? = null, tag: String? = null, animate: Boolean = false, viewId: Int = 0, singleton: Boolean = false) {
        val id = if (viewId == 0) rootView?.id ?: 0 else viewId
        navigationController?.pushFragment(fragment, bundle = bundle, tag = tag, animate = animate, viewId = id, singleton = singleton)
    }

    fun pushChildFragment(clazz: KClass<out Fragment>, bundle: Bundle? = null, tag: String? = null, viewId: Int = 0, singleton: Boolean = false) {
        pushChildFragment(fragment = clazz.java.newInstance(), bundle = bundle, tag = tag, viewId = viewId, singleton = singleton)
    }

    fun addChildFragment(viewId: Int, clazz: KClass<out Fragment>, bundle: Bundle? = null, tag: String? = null, singleton: Boolean = false) {
        val id = if (viewId == 0) rootView?.id ?: 0 else viewId
        navigationController?.addFragment(id, clazz = clazz, bundle = bundle, tag = tag, singleton = singleton)
    }

    fun removeChildFragment(fragment: Fragment) {
        navigationController?.removeFragment(fragment)
    }

    fun removeChildFragment(tag: String) {
        navigationController?.removeFragment(tag)
    }

    fun removeChildFragment(clazz: KClass<out Fragment>) {
        navigationController?.removeFragment(clazz)
    }

    fun <T: Fragment>getParentFragment(clazz: KClass<T>) : T? {
        return findFragment(clazz, arguments?.getString(NavigationController.PARENT_FRAGMENT_NAME_TAG))
    }

    fun setUpHidingBottomNavigationViewOnMainActivity(recyclerView: RecyclerView) {
        val activity = (activity as MainActivity?)
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    activity?.showBottomTabs(if (recyclerView.canScrollVertically(1)) 1_000 else 0)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                activity?.showOrHideBottomTabs(dy > 0)
            }
        })
    }

    val fromFragment: BaseFragment?
        get() = arguments?.getString(PARENT_FRAGMENT_NAME_TAG)?.let {
            findFragment(BaseFragment::class, tag = it)
        }

    open fun scrollToItemPosition(position: Int, item: WallpaperModel) { }

    fun hideSoftKeyBoard(){
        val inputMethodManager: InputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
        activity?.currentFocus?.clearFocus()
    }

}
