package com.calltheme.app.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.screentheme.app.databinding.LoadingAdsDialogLayoutBinding


class DialogLoadingAds : DialogFragment() {

    lateinit var binding: LoadingAdsDialogLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = LoadingAdsDialogLayoutBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        val dialog = builder.create()



        isCancelable = false

        return dialog
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, "LoadingAdsDialog")
    }
}
