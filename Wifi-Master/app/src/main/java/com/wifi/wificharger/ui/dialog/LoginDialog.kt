package com.wifi.wificharger.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.wifi.wificharger.databinding.DialogLoginBinding

class LoginDialog : DialogFragment() {
    private lateinit var binding: DialogLoginBinding
    private var onLogin: (String) -> Unit = {}
    private var name = ""

    companion object {
        const val TAG = "LoginDialog"
        fun newInstance(wifiName: String): LoginDialog {
            val dialog = LoginDialog()
            dialog.name = wifiName
            dialog.setStyle(STYLE_NO_TITLE, 0)
            dialog.isCancelable = false
            return dialog
        }
    }

    fun show(manager: FragmentManager, onLogin: (String) -> Unit) {
        this.onLogin = onLogin
        show(manager, TAG)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogLoginBinding.inflate(inflater, container, false)
        if (name.isEmpty().not()) {
            binding.tvTitle.text = name
        }
        binding.dialogLoginLoginBtn.setOnClickListener {
            val password = binding.dialogLoginPassword.text.toString()
            onLogin.invoke(password)
            dismissAllowingStateLoss()
//            if (activity != null) {
//                val passwordFromShared = "Your password";
//
//                assert(passwordFromShared != null)
//                if (password.contains(passwordFromShared) && password.length == passwordFromShared.length) {
//                    dismissAllowingStateLoss()
//                } else {
//                    //show some info when error
//                }
//            }
        }
        binding.dialogCancel.setOnClickListener {
            dismissAllowingStateLoss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}