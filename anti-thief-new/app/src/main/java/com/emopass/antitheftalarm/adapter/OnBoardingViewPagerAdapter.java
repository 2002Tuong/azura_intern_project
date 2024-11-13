package com.emopass.antitheftalarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.OnboardingViewBinding;
import com.emopass.antitheftalarm.model.OnBoardingModel;
import java.util.ArrayList;
import java.util.List;

public class OnBoardingViewPagerAdapter extends RecyclerView.Adapter<OnBoardingViewPagerAdapter.ViewHolder> {
    private Context context;
    private List<OnBoardingModel> onBoardingModelList = new ArrayList<>();
    public OnBoardingViewPagerAdapter(Context context) {
        this.context = context;
        onBoardingModelList.add(new OnBoardingModel(context.getString(R.string.onboarding1_title), R.drawable.front_onboarding_screen_1));
        onBoardingModelList.add(new OnBoardingModel(context.getString(R.string.onboarding2_title), R.drawable.front_onboarding_screen_2));
        onBoardingModelList.add(new OnBoardingModel(context.getString(R.string.onboarding3_title), R.drawable.front_onboarding_screen_3));

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OnboardingViewBinding binding = OnboardingViewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return onBoardingModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private OnboardingViewBinding binding;
        public ViewHolder(OnboardingViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public OnboardingViewBinding getBinding() {
            return binding;
        }

        public void onBind(int position) {
            OnBoardingModel model = onBoardingModelList.get(position);
            if(position == 0) {
                binding.imgOnBoarding.setScaleType(ImageView.ScaleType.FIT_START);
            }
            binding.imgOnBoarding.setImageResource(model.getImageRes());
            binding.txtTitle.setText(model.getLabel());
        }
    }
}
