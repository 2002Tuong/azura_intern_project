package com.slideshowmaker.slideshow.ui.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.databinding.ViewOnboardBinding
import com.slideshowmaker.slideshow.models.OnBoardingModel

class OnBoardingViewPagerAdapter(val context: Context) :
    RecyclerView.Adapter<OnBoardingViewPagerAdapter.PageViewHolder>() {

    private val onBoardingDataList = listOf(
        OnBoardingModel(
            context.getString(R.string.title_onboard_1),
            context.getString(R.string.description_onboard_1), R.drawable.onboard1
        ),
        OnBoardingModel(
            context.getString(R.string.title_onboard_2),
            context.getString(R.string.description_onboard_2), R.drawable.onboard2
        ),
        OnBoardingModel(
            context.getString(R.string.title_onboard_3),
            context.getString(R.string.description_onboard_3), R.drawable.onboard3
        )
    )

    inner class PageViewHolder(private val binding: ViewOnboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(position: Int) {
            val onBoardingData = onBoardingDataList[position]
            binding.imgOnBoarding.setImageResource(onBoardingData.image)
            binding.txtDescription.text = onBoardingData.description
            binding.txtTitle.text = onBoardingData.title
        }
    }

    override fun getItemCount(): Int = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return PageViewHolder(
            ViewOnboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.onBind(position)
    }
}
