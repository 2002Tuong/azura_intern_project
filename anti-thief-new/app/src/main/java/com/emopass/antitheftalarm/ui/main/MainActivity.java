package com.emopass.antitheftalarm.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.ads.control.Rate;
import com.ads.control.funtion.UtilsApp;
import com.emopass.antitheftalarm.App;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.ActivityMainBinding;
import com.emopass.antitheftalarm.databinding.AppBarMainBinding;
import com.emopass.antitheftalarm.ui.BaseActivity;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.IronSource;

public class MainActivity extends BaseActivity<ActivityMainBinding> {

  private AppBarMainBinding appBarMainBinding;
  private AppBarConfiguration mAppBarConfiguration;
  private boolean isHomeScreen;
  private FirebaseAnalytics firebaseAnalytics;

  @Override
  protected ActivityMainBinding getViewBinding() {
    return ActivityMainBinding.inflate(LayoutInflater.from(this));
  }

  @Override
  protected void initView() {
    firebaseAnalytics = FirebaseAnalytics.getInstance(this);

    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "open_main_screen");
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

    appBarMainBinding = binding.appBarMain;
    setSupportActionBar(appBarMainBinding.toolbar);
    appBarMainBinding.toolbar.setTitleTextColor(
        getResources().getColor(android.R.color.transparent));
    mAppBarConfiguration =
        new AppBarConfiguration.Builder(R.id.nav_home).setOpenableLayout(binding.drawerLayout)
            .build();
    NavController navController = ((NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();
    NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
    NavigationUI.setupWithNavController(binding.navView, navController);

    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
      switch (destination.getId()) {
        case R.id.nav_home:
          isHomeScreen = true;
          break;
        default:
          isHomeScreen = false;
          break;
      }
    });
  }

  @Override
  public void onBackPressed() {
      if (isHomeScreen) {
          Rate.Show(this, 1);
      } else {
          super.onBackPressed();
      }
  }

  @Override
  protected void initControl() {
    binding.navView.setNavigationItemSelectedListener(item -> {
      App.getInstace().setForceLockScreen(true);
      switch (item.getItemId()) {
        case R.id.menu_feedback:
          UtilsApp.SendFeedBack(this, getString(R.string.email_feedback),
              getString(R.string.Title_email));
          break;
        case R.id.menu_more_app:
          UtilsApp.OpenBrower(this, getString(R.string.link_more_app));
          break;
        case R.id.menu_policy:
          UtilsApp.OpenBrower(this, getString(R.string.link_policy));
          break;
        case R.id.menu_rateus:
          UtilsApp.RateApp(this);
          break;
        case R.id.menu_share:
          UtilsApp.shareApp(this);
          break;
      }
      binding.drawerLayout.closeDrawers();
      return true;
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    IronSource.onResume(this);

    //new Handler().postDelayed(() -> {
    //    if (App.getInstace().getTopActivity() instanceof MainActivity) {
    //        if (!BackgroundManager.getInstance(this).isServiceRunning(KeepLiveAccessibilityService.class)) {
    //            requestDetectHome();
    //        }
    //    }
    //}, 3000);

    //if (!App.getInstace().isForceLockScreen()) {
    //  Intent intent = new Intent(this, PasswordActivity.class);
    //  intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    //  if (PreferencesHelper.isPatternCode()) {
    //    intent.setAction(Config.ActionIntent.ACTION_CHECK_PATTERN_CODE);
    //  } else {
    //    intent.setAction(Config.ActionIntent.ACTION_CHECK_PIN_CODE);
    //  }
    //  startActivity(intent);
    //}
    //App.getInstace().setForceLockScreen(false);
  }

  public void setStageDrawerLayout(boolean isLock) {
    binding.drawerLayout.setDrawerLockMode(
        isLock ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
  }

  @Override
  protected void onPause() {
    super.onPause();
    IronSource.onPause(this);
  }

  @Override
  public boolean onSupportNavigateUp() {
    NavController navController = ((NavHostFragment) (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))).getNavController();
    return NavigationUI.navigateUp(navController, mAppBarConfiguration)
        || super.onSupportNavigateUp();
  }

  @Override
  protected View getViewPadding() {
    return binding.drawerLayout;
  }

  @Override
  protected TextView getToolbarTitle() {
    return appBarMainBinding.toolbarTitle;
  }
}
