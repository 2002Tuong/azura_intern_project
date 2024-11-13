package com.emopass.antitheftalarm.ui.onboarding;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;
import com.ads.control.ads.VioAdmob;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.adapter.OnBoardingViewPagerAdapter;
import com.emopass.antitheftalarm.databinding.ActivityOnBoardingBinding;
import com.emopass.antitheftalarm.ui.BaseActivity;
import com.emopass.antitheftalarm.ui.password.PasswordActivity;
import com.emopass.antitheftalarm.utils.AdsUtils;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.LanguageUtils;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ironsource.mediationsdk.IronSource;

public class OnBoardingActivity extends BaseActivity<ActivityOnBoardingBinding> {
    private int currentPageIndex = 0;
    private ApNativeAd onboarding1 = null;
    private ApNativeAd onboarding2 = null;
    private ApNativeAd onboarding3 = null;
    @Override
    protected void initView() {
        LanguageUtils.getInstance().loadLocale(this);
        AdsUtils.getInstance().requestNativeOnboard(this);
        hideNavigationBar();
        setupViewPager();
        binding.txtContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextPage();
            }
        });
    }

    @Override
    protected ActivityOnBoardingBinding getViewBinding() {
        return ActivityOnBoardingBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initControl() {
        initNative();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            hideNavigationBar();
        }
    }

    private void setupViewPager() {
        binding.vpTutorial.setAdapter(new OnBoardingViewPagerAdapter(this));
        binding.vpTutorial.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPageIndex = position;
                binding.txtContinue.setText(getString(R.string.next));

                switch (position) {
                    case 0:
                        if(onboarding1 != null) {
                            VioAdmob.getInstance().populateNativeAdView(
                                    OnBoardingActivity.this,
                                    onboarding1,
                                    binding.frAds,
                                    binding.includeNative.shimmerContainerBanner
                            );
                        }
                        break;
                    case 1:
                        if(onboarding2 != null) {
                            VioAdmob.getInstance().populateNativeAdView(
                                    OnBoardingActivity.this,
                                    onboarding2,
                                    binding.frAds,
                                    binding.includeNative.shimmerContainerBanner
                            );
                        }
                        break;
                    case 2:
                        if(onboarding3 != null) {
                            VioAdmob.getInstance().populateNativeAdView(
                                    OnBoardingActivity.this,
                                    onboarding3,
                                    binding.frAds,
                                    binding.includeNative.shimmerContainerBanner
                            );
                        }
                        break;
                }
            }
        });
        new TabLayoutMediator(binding.tlTutorial, binding.vpTutorial, (v1, v2) -> {}).attach();
    }

    private int getItem() {
        return binding.vpTutorial.getCurrentItem();
    }

    private void onNextPage() {
        currentPageIndex ++;
        binding.txtContinue.setText(R.string.next);
        if(currentPageIndex < 3) {
            binding.vpTutorial.setCurrentItem(currentPageIndex, true);
        } else if(currentPageIndex == 3) {
            currentPageIndex--;
            launchHome();
        } else {
            launchHome();
        }
    }

    private void launchHome() {
       new Handler().postDelayed(this::gotoSetUpPassword,100);
    }

    private void gotoSetUpPassword() {
        Intent intent = new Intent(this, PasswordActivity.class);
        intent.putExtra(Config.KeyBundle.KEY_FIRST_SETUP_PASS, true);
        intent.setAction(Config.ActionIntent.ACTION_SET_UP_PATTERN_CODE);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void hideNavigationBar() {
        int layoutInDisplayCutoutModeDefault = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        int flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        getWindow().getDecorView().setSystemUiVisibility(flags);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == 0) {
                    getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            }
        });
    }

    private void initNative() {
        AdsUtils.nativeOnboard1.observe(this, apNativeAd -> {
            if(apNativeAd != null) {
                onboarding1 = apNativeAd;
                if(getItem() == 0) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        VioAdmob.getInstance().populateNativeAdView(
                                OnBoardingActivity.this,
                                onboarding1,
                                binding.frAds,
                                binding.includeNative.shimmerContainerBanner
                        );
                    }) ;
                }
            }
        });

        AdsUtils.nativeOnboard2.observe(this, apNativeAd -> {
            if(apNativeAd != null) {
                onboarding2 = apNativeAd;
                if(getItem() == 1) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        VioAdmob.getInstance().populateNativeAdView(
                                OnBoardingActivity.this,
                                onboarding2,
                                binding.frAds,
                                binding.includeNative.shimmerContainerBanner
                        );
                    }) ;
                }
            }
        });

        AdsUtils.nativeOnboard3.observe(this, apNativeAd -> {
            if(apNativeAd != null) {
                onboarding3 = apNativeAd;
                if(getItem() == 2) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        VioAdmob.getInstance().populateNativeAdView(
                                OnBoardingActivity.this,
                                onboarding3,
                                binding.frAds,
                                binding.includeNative.shimmerContainerBanner
                        );
                    }) ;
                }
            }
        });

        AdsUtils.nativeOnboardLoadFail.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    binding.frAds.setVisibility(View.GONE);
                    AdsUtils.nativeOnboardLoadFail.postValue(false);
                }
            }
        });
    }
}
