package com.example.videoart.batterychargeranimation.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.videoart.batterychargeranimation.databinding.FragmentOnboarding1Binding

class OnBoarding1Fragment : Fragment() {
    private var binding: FragmentOnboarding1Binding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboarding1Binding.inflate(layoutInflater)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        fun newInstance(): OnBoarding1Fragment {
            val fragment = OnBoarding1Fragment()
            return fragment
        }
    }

}