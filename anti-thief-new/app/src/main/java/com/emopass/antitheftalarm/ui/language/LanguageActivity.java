package com.emopass.antitheftalarm.ui.language;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ads.control.ads.VioAdmob;
import com.emopass.antitheftalarm.adapter.LanguageAdapter;
import com.emopass.antitheftalarm.databinding.ActivityLanguageBinding;
import com.emopass.antitheftalarm.model.LanguageModel;
import com.emopass.antitheftalarm.ui.BaseActivity;
import com.emopass.antitheftalarm.ui.onboarding.OnBoardingActivity;
import com.emopass.antitheftalarm.utils.AdsUtils;
import com.emopass.antitheftalarm.utils.LanguageUtils;
import com.ironsource.mediationsdk.IronSource;

import java.util.List;

public class LanguageActivity extends BaseActivity<ActivityLanguageBinding> {
    LanguageAdapter languageAdapter;
    private LanguageViewModel viewModel = null;
    private Boolean isFromSetting = false;
    @Override
    protected void initView() {
        LanguageUtils.getInstance().loadLocale(this);
        AdsUtils.getInstance().requestNativeLanguage(this, false);
        isFromSetting = getIntent().getBooleanExtra(FROM_SETTING_KEY, false);
        viewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
        setUpAdapter();

    }

    @Override
    protected ActivityLanguageBinding getViewBinding() {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initControl() {
        binding.imgChoose.setOnClickListener(v -> {
            LanguageUtils.getInstance().changeLang(languageAdapter.itemSelected().getLanguageCode(), this);
            if(!isFromSetting) {
                launchOnBoard();
            }
        });
        binding.imgBack.setOnClickListener(v -> {
            onBackPressed();
        });
        viewModel.selectedLanguage.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                int visible;
                if(s.isEmpty() || s == null) {
                    visible = View.GONE;
                }else {
                    visible = View.VISIBLE;
                }
                binding.imgChoose.setVisibility(visible);
            }
        });
        if(isFromSetting) {
            binding.frAds.setVisibility(View.GONE);
            binding.imgBack.setVisibility(View.VISIBLE);
        }else {
            binding.imgBack.setVisibility(View.GONE);
            initAds();
            hideNavigationBar();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && !isFromSetting) {
            hideNavigationBar();
        }
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

    private void setUpAdapter() {
        languageAdapter = new LanguageAdapter(this);
        languageAdapter.setOnLanguageChange(new LanguageAdapter.OnLanguageChange() {
            @Override
            public void onChange(String language) {
                viewModel.selectLanguage(language);
            }
        });
        List<LanguageModel> listLang = LanguageUtils.getInstance().getSupportedLanguages();
        if(!isFromSetting) {
            listLang = listLang.subList(0,5);
        }
        languageAdapter.setData( listLang);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvcLanguage.addItemDecoration(new SpaceDecorator(8));
        binding.rvcLanguage.setLayoutManager(layoutManager);
        binding.rvcLanguage.setAdapter(languageAdapter);
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

    private void launchOnBoard() {
        Intent intent = new Intent(this, OnBoardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void initAds() {
        AdsUtils.nativeLanguageLoadFail.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    binding.frAds.setVisibility(View.GONE);
                }
            }
        });

        AdsUtils.nativeLanguage.observe(this, apNativeAd -> {
            if(apNativeAd != null) {
                VioAdmob.getInstance().populateNativeAdView(
                        LanguageActivity.this,
                        apNativeAd,
                        binding.frAds,
                        binding.includeNative.shimmerContainerBanner);
            }
        });
    }
    private static String FROM_SETTING_KEY = "from setting";
    public static void start(Activity activity, Boolean fromSetting) {
        Intent intent = new Intent(activity, LanguageActivity.class);
        intent.putExtra(FROM_SETTING_KEY, fromSetting);
        activity.startActivity(intent);
        if(!fromSetting) {
            activity.finish();
        }
    }
}
