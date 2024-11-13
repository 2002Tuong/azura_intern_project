package com.calltheme.app.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ads.control.admob.AppOpenManager
import com.calltheme.app.ui.activity.MainActivity
import com.screentheme.app.R
import com.screentheme.app.databinding.PermissionsDialogLayoutBinding
import com.screentheme.app.utils.extensions.arePermissionsGranted
import com.screentheme.app.utils.extensions.setBackgroundAndKeepPadding
import com.screentheme.app.utils.extensions.setWidthPercent
import com.screentheme.app.utils.helpers.handlePermissions
import com.screentheme.app.utils.helpers.isXiaomiDevice
import com.screentheme.app.utils.helpers.permissionList
import com.screentheme.app.utils.helpers.requestWriteSettingsPermission
import com.screentheme.app.utils.helpers.requestXiaomiPermissions

class PermissionsPopup(
    private val isShowXiaomiPermission: Boolean = false
) : DialogFragment() {

    lateinit var binding: PermissionsDialogLayoutBinding
    var onDismiss: () -> Unit = {}
    var onShowingRequestDialog: () -> Unit = {}
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = PermissionsDialogLayoutBinding.inflate(inflater, container, false)
//        return binding.root
//    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//
//    }

    private fun setupViews() {

        binding.apply {
            permsSwitch.isChecked =
                requireActivity().arePermissionsGranted(permissions = permissionList())
            systemSettingSwitch.isChecked = Settings.System.canWrite(requireContext())

            permsSwitch.setOnClickListener {
                AppOpenManager.getInstance().disableAdResumeByClickAction()
                val mainActivity = activity as MainActivity
                if (!permsSwitch.isChecked) {
                    onShowingRequestDialog.invoke()
                }
                mainActivity.handlePermissions(permissionList()) { granted ->
                    permsSwitch.isChecked = granted
                }
            }

            systemSettingSwitch.setOnClickListener {
                AppOpenManager.getInstance().disableAdResumeByClickAction()
                val mainActivity = activity as MainActivity
                mainActivity.requestWriteSettingsPermission { granted ->
                    systemSettingSwitch.isChecked = granted
                }
            }

            if (isXiaomiDevice() && isShowXiaomiPermission) {
                layoutXiaoMiPermissions.visibility = View.VISIBLE
            } else {
                layoutXiaoMiPermissions.visibility = View.GONE
            }

            xiaomiPermSwitch.setOnClickListener {
                AppOpenManager.getInstance().disableAdResumeByClickAction()
                val mainActivity = activity as MainActivity
                mainActivity.requestXiaomiPermissions { granted ->
                    xiaomiPermSwitch.isChecked = granted

                }
            }

            binding.btnClose.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isAdded) return
        super.show(manager, tag)
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismiss.invoke()
        super.onDismiss(dialog)
    }

    override fun onResume() {
        super.onResume()
        setWidthPercent(1f)
    }

    private fun setActivatedButton(button: Button, isActivated: Boolean) {
        if (isActivated) {
            button.isEnabled = false
            button.setBackgroundAndKeepPadding(R.drawable.bg_round_granted_button)
            button.setTextColor(Color.TRANSPARENT)
        } else {
            button.isEnabled = true
            button.setBackgroundAndKeepPadding(R.drawable.ungranted_rounded_button_bg)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = PermissionsDialogLayoutBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        val dialog = builder.create()
        setupViews()

        return dialog
    }
}
