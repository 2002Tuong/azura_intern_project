package com.wifi.wificharger.ui.genpw

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.view.isVisible
import com.wifi.wificharger.data.model.FeatureTitle
import com.wifi.wificharger.databinding.FragmentGeneratePasswordBinding
import com.wifi.wificharger.ui.base.BaseFragment
import com.wifi.wificharger.ui.base.BaseViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.security.SecureRandom

class GeneratePassword : BaseFragment<FragmentGeneratePasswordBinding, BaseViewModel>(
    FragmentGeneratePasswordBinding::inflate
) {

    override val viewModel: BaseViewModel by viewModel()

    override fun initView() {
        with(viewBinding) {
            navBar.navTitle.text = FeatureTitle.GENERATE_PASSWORD.title
            navBar.goBackButton.isVisible = true
            navBar.goBackButton.setOnClickListener {
                navigateUp()
            }
            layoutUpperCase.tvTitle.text = "Upper Case"
            layoutLowerCase.tvTitle.text = "Lower Case"
            layoutNumberCase.tvTitle.text = "Numbers"
            layoutSymbolCase.tvTitle.text = "Symbols"
            btnGen.setOnClickListener {
                if (!layoutUpperCase.cbToggle.isChecked && !layoutNumberCase.cbToggle.isChecked && !layoutSymbolCase.cbToggle.isChecked && !layoutLowerCase.cbToggle.isChecked) {
                    Toast.makeText(context, "You must select at least one options", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.text = generatePassword(
                    length = seekbarLength.progress,
                    requireLowercase = layoutLowerCase.cbToggle.isChecked,
                    requireUppercase = layoutUpperCase.cbToggle.isChecked,
                    requireDigits = layoutNumberCase.cbToggle.isChecked,
                    requireSymbols = layoutSymbolCase.cbToggle.isChecked
                )
            }
            btnCopy.setOnClickListener {
                copyToClipBoard("password", password.text)
            }
            btnShare.setOnClickListener {
                shareToOtherApp(password.text)
            }
        }
    }

    private fun generatePassword(
        length: Int = 8,
        requireLowercase: Boolean = false,
        requireUppercase: Boolean = false,
        requireDigits: Boolean = false,
        requireSymbols: Boolean = false
    ): String {
        var result = ""
        if (requireLowercase) result += ('a'..'z').random()
        if (requireUppercase) result += ('A'..'Z').random()
        if (requireDigits) result += ('0'..'9').random()
        if (requireSymbols) result += listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')').random()

        val charset = if (requireLowercase) ('a'..'z') else emptyList<Char>() +
                    if (requireUppercase) ('A'..'Z') else emptyList<Char>() +
                    if (requireDigits) ('0'..'9') else emptyList<Char>() +
                    if (requireSymbols) listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')') else emptyList()
        val secureRandom = SecureRandom()
         result += (1..length - result.length)
            .map { charset.shuffled()[secureRandom.nextInt(charset.count())] }
            .joinToString("")
        result.toCharArray().shuffle()
        return result
    }

    private fun copyToClipBoard(label: String, text: CharSequence) {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipData = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clipData)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareToOtherApp(shareBody: CharSequence) {
        /*Create an ACTION_SEND Intent*/
        val intent = Intent(Intent.ACTION_SEND)
        /*The type of the content is text, obviously.*/
        intent.setType("text/plain")
        /*Applying information Subject and Body.*/
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            "Share"
        )
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(intent, "Share Using"))
    }

    override fun observeData() {
        // TODO("Not yet implemented")
    }

    override fun loadAds() {
        // TODO("Not yet implemented")
    }
}