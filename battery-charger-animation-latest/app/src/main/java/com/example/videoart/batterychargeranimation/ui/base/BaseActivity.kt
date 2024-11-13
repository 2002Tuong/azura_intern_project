package com.example.videoart.batterychargeranimation.ui.base

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import com.example.videoart.batterychargeranimation.ui.dialog.ErrorPopupFragment

abstract class BaseActivity : AppCompatActivity(){
    companion object {
        val PICK_IMAGE_REQUEST = 1
    }
    var onPickupImageResultCallBack: ((uri: Uri?) -> Unit)? = null
    private var errorDialogFrag: DialogFragment? = null
    fun pickImageFromGallery(callback: (selectedImageUri: Uri?) -> Unit) {

        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)

        onPickupImageResultCallBack = {
            callback(it)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            Log.d("PickMedia", "${selectedImageUri}")
            Log.d("PickMedia", "${onPickupImageResultCallBack != null}")
            onPickupImageResultCallBack?.invoke(selectedImageUri)
        }
    }
}