package com.emopass.antitheftalarm.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.data.remote.RemoteConfig;
import com.emopass.antitheftalarm.databinding.ActivitySplashBinding;
import com.emopass.antitheftalarm.ui.BaseActivity;
import com.emopass.antitheftalarm.ui.language.LanguageActivity;
import com.emopass.antitheftalarm.ui.main.MainActivity;
import com.emopass.antitheftalarm.ui.password.PasswordActivity;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.ironsource.mediationsdk.IronSource;

public class SplashActivity extends BaseActivity<ActivitySplashBinding> {

    private  SplashViewModel viewModel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RemoteConfig instance = RemoteConfig.getInstance();
        instance.fetch(this);
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        viewModel.navigateToMainScreenState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    if(!PreferencesHelper.isFirstTimeSetupComplete()) {
                        LanguageActivity.start(SplashActivity.this, false);
                    } else {
                        openMainScreen();
                    }
                }
            }
        });

        instance.isFetchComplete.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewModel.loadAppOpen(SplashActivity.this);
            }
        });
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
        viewModel.checkShowOpenAdSplashLoadFail(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void initView() {
        findViewById(R.id.parentView).setVisibility(View.VISIBLE);
    }

    private void openMainScreen() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    private void gotoSetUpPassword() {
        Intent intent = new Intent(this, PasswordActivity.class);
        intent.putExtra(Config.KeyBundle.KEY_FIRST_SETUP_PASS, true);
        intent.setAction(Config.ActionIntent.ACTION_SET_UP_PATTERN_CODE);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected ActivitySplashBinding getViewBinding() {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initControl() {

    }
}
