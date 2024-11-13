package com.emopass.antitheftalarm.ui.password;

import android.view.LayoutInflater;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.ActivityPasswordBinding;
import com.emopass.antitheftalarm.ui.BaseActivity;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.SystemUtil;
import com.ironsource.mediationsdk.IronSource;

public class PasswordActivity extends BaseActivity<ActivityPasswordBinding> {

    private boolean isCanBack = true;
    private boolean isFirstSetupPass;
    public boolean isFirstSetupPass() {
        return isFirstSetupPass;
    }

    @Override
    protected ActivityPasswordBinding getViewBinding() {
        return ActivityPasswordBinding.inflate(LayoutInflater.from(this));
    }

    @Override
    protected void initView() {
        if (getIntent() != null || getIntent().getAction() != null) {
            isCanBack = (getIntent().getAction().contains(Config.ActionIntent.ACTION_CHANGE_PATTERN_CODE) ||
                    getIntent().getAction().contains(Config.ActionIntent.ACTION_CHANGE_PIN_CODE) ||
                    getIntent().getAction().contains(Config.ActionIntent.ACTION_SWITCH_TO_PIN_CODE) ||
                    getIntent().getAction().contains(Config.ActionIntent.ACTION_SWITCH_TO_PATTERN_CODE));

        }
        if (getIntent() != null)
            isFirstSetupPass = getIntent().getBooleanExtra(Config.KeyBundle.KEY_FIRST_SETUP_PASS, false);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(isCanBack);
        getSupportActionBar().setDisplayShowHomeEnabled(isCanBack);
        binding.toolbar.setTitleTextColor(getResources().getColor(android.R.color.transparent));

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHost.getNavController();
        NavInflater navInflater = navController.getNavInflater();
        NavGraph graph = navInflater.inflate(R.navigation.password_navigation);
        graph.setStartDestination(PreferencesHelper.isPatternCode() ? R.id.nav_pattern_code : R.id.nav_pin_code);
        navController.setGraph(graph);
    }

    @Override
    protected void initControl() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        if (isCanBack) {
            onBackPressed();
            return true;
        }
        return false;
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
    public void onBackPressed() {
        if (isCanBack) {
            super.onBackPressed();
        } else {
            SystemUtil.gotoHomeLauncher(this);
        }
    }

    @Override
    protected View getViewPadding() {
        return binding.container;
    }
}
