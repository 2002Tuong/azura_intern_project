package com.example.claptofindphone.presenter.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.OnboardingViewBinding
import com.example.claptofindphone.models.OnBoardingPageData


class OnBoardingViewPagerAdapter(val context: Context) :
    RecyclerView.Adapter<OnBoardingViewPagerAdapter.PageViewHolder>() {

    private val onBoardingPageDataLists = listOf(
        OnBoardingPageData(
            context.getString(R.string.onboarding_title_1),
            "", R.drawable.front_onboarding_screen_1, R.drawable.bg_onboarding_screen_1
        ),
        OnBoardingPageData(
            context.getString(R.string.onboarding_title_2),
            "", R.drawable.front_onboarding_screen_2, R.drawable.bg_onboarding_screen_2
        ),
        OnBoardingPageData(
            context.getString(R.string.onboarding_title_3),
            "", R.drawable.front_onboarding_screen_3, R.drawable.bg_onboarding_screen_3
        )
    )

    inner class PageViewHolder(private val binding: OnboardingViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val onBoardingPageData = onBoardingPageDataLists[position]
            if(position == 0) {
                binding.imgOnBoarding.scaleType = ImageView.ScaleType.FIT_START
            }
            binding.imgOnBoarding.setImageResource(onBoardingPageData.imageResId)
            //binding.background.setImageResource(onBoardingModel.background)
            binding.txtDescription.text = onBoardingPageData.subLabel
            binding.txtTitle.text = onBoardingPageData.label
        }
    }

    override fun getItemCount(): Int = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return PageViewHolder(
            OnboardingViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.onBind(position)
    }
}
