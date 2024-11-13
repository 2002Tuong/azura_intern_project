package com.calltheme.app.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.admob.AppOpenManager
import com.ironsource.mediationsdk.IronSource
import com.screentheme.app.utils.extensions.isShowOnLockScreenPermissionEnable

abstract class BaseActivity : AppCompatActivity() {
    companion object {
        val PICK_IMAGE_REQUEST = 1
    }


    var onRequestPermissionsResultCallback: ((requestCode: Int, permissions: Array<out String>, grantResults: IntArray) -> Unit)? = null
    var onPickupImageResultCallBack: ((uri: Uri?) -> Unit)? = null
    var onRequestDialerCallBack: ((granted: Boolean) -> Unit)? = null
    var onRequestWriteSettingsPermissionCallback: ((granted: Boolean) -> Unit)? = null
    var onRequestXiaomiPermissionCallback: ((granted: Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onResume() {
        super.onResume()

        onRequestXiaomiPermissionCallback?.let { it(this.isShowOnLockScreenPermissionEnable()) }
        IronSource.onResume(this)
    }

    public override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    fun pickImageFromGallery(callback: (selectedImageUri: Uri?) -> Unit) {
        AppOpenManager.getInstance().disableAppResume()
        val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intentGallery.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

        intentGallery.type = "image/*"
        startActivityForResult(intentGallery, PICK_IMAGE_REQUEST)

        onPickupImageResultCallBack = {
            callback(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val pickedImageUri: Uri? = data.data
            onPickupImageResultCallBack?.invoke(pickedImageUri)
        }
        if (requestCode == PICK_IMAGE_REQUEST)
            AppOpenManager.getInstance().enableAppResume()
    }

    fun hideNavigationBar() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                window.decorView.systemUiVisibility = flags
            }
        }
    }
}