package com.calltheme.app.ui.thankyou

import android.os.Handler
import androidx.activity.addCallback
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.base.BaseFragment
import com.screentheme.app.databinding.FragmentThankYouBinding

class ThankYouFragment : BaseFragment() {

    lateinit var binding: FragmentThankYouBinding

    override fun getViewBinding(): ViewBinding {
        binding = FragmentThankYouBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        Handler().postDelayed({
            activity?.finish()
        }, 1500)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

        }
    }

    override fun registerObservers() {
    }

}