package com.example.videoart.batterychargeranimation.ui.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.ViewOnboardBinding
import com.example.videoart.batterychargeranimation.model.OnboardModel

class OnBoardingViewPagerAdapter(val context: Context) : RecyclerView.Adapter<OnBoardingViewPagerAdapter.ViewHolder>() {

    private val listOnboardingModel = listOf(
        OnboardModel(R.drawable.onboarding1, context.getString(R.string.onboard_title1), ""),
        OnboardModel(R.drawable.onboarding2, context.getString(R.string.onboard_title2), ""),
        OnboardModel(R.drawable.onboarding3, context.getString(R.string.onboard_title3), "")
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnBoardingViewPagerAdapter.ViewHolder {
        val binding = ViewOnboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnBoardingViewPagerAdapter.ViewHolder, position: Int) {
        val model = listOnboardingModel[position]
        holder.bind(model)
    }

    override fun getItemCount(): Int {
        return listOnboardingModel.size
    }

    inner class ViewHolder(val binding: ViewOnboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: OnboardModel) {
            binding.imgOnBoarding.setImageResource(model.backgroundId)
            binding.txtTitle.text = model.title
            binding.txtDescription.text = model.subTitle
        }
    }
}