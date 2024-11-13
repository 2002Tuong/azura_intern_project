package com.emopass.antitheftalarm.ui.setting;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import com.ads.control.ads.VioAdmob;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.emopass.antitheftalarm.App;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.FragmentSettingBinding;
import com.emopass.antitheftalarm.dialog.DialogSelectItem;
import com.emopass.antitheftalarm.dialog.DialogSelectLanguage;
import com.emopass.antitheftalarm.dialog.DialogSound;
import com.emopass.antitheftalarm.model.SelectModel;
import com.emopass.antitheftalarm.ui.BaseFragment;
import com.emopass.antitheftalarm.utils.AdsUtils;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SetttingFragment extends BaseFragment<FragmentSettingBinding> {

  private boolean goToLockOption = false; // Uses to not show ads when going to lock option
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
      AdsUtils.getInstance().requestNativeSetting(requireActivity(), false);
  }

  @Override
    protected void initView() {
        new Handler().postDelayed(() -> {
            boolean isFullCharger = PreferencesHelper.getBoolean(PreferencesHelper.FULL_CHARGER_ALARM, false);
            binding.funcFullCharger.setSwChecked(isFullCharger);

            boolean isChargerConnected = PreferencesHelper.getBoolean(PreferencesHelper.NOTI_CHARGER_CONNECTED, false);
            binding.funcChargerConnected.setSwChecked(isChargerConnected);

            boolean isVibrateAlarm = PreferencesHelper.getBoolean(PreferencesHelper.VIBRATE_DURING_ALARM, true);
            binding.funcVibrate.setSwChecked(isVibrateAlarm);

            boolean isFlashAlarm = PreferencesHelper.getBoolean(PreferencesHelper.FLASH_DURING_ALARM, false);
            binding.funcFlashlight.setSwChecked(isFlashAlarm);
        }, 50);

        // Analytics
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "open_settings_screen");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.getClass().getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.getClass().getSimpleName());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @Override
    public void onDestroyView() {
      goToLockOption = false;
      super.onDestroyView();
    }

    @Override
    protected void initControl() {
        binding.funcLockOption.setItemClickListener(() -> {
            navigate(R.id.action_setting_to_lock_options);
            goToLockOption = true;
        });
        binding.funcTimer.setItemClickListener(this::showDialogSetTime);

        binding.funcAlarmTone.setItemClickListener(this::showDialogAlarmTone);

        binding.funcSound.setItemClickListener(this::showDialogSound);

        binding.funcFullCharger.setActionListener(isChecked -> {
            PreferencesHelper.putBoolean(PreferencesHelper.FULL_CHARGER_ALARM, isChecked);
        });

        binding.funcChargerConnected.setActionListener(isChecked -> {
            PreferencesHelper.putBoolean(PreferencesHelper.NOTI_CHARGER_CONNECTED, isChecked);
        });

        binding.funcVibrate.setActionListener(isChecked -> {
            PreferencesHelper.putBoolean(PreferencesHelper.VIBRATE_DURING_ALARM, isChecked);
        });

        binding.funcFlashlight.setActionListener(isChecked -> {
            PreferencesHelper.putBoolean(PreferencesHelper.FLASH_DURING_ALARM, isChecked);
        });

        binding.funcLanguage.setItemClickListener(() -> new DialogSelectLanguage.ExtendBuilder()
                .setTitle(getString(R.string.change_language))
                .onSetNegativeButton(getString(R.string.cancel), baseDialog -> baseDialog.dismiss())
                .onSetPositiveButton(getString(R.string.ok), (baseDialog, datas) -> {
                    App.getInstace().setForceLockScreen(true);
                    getActivity().finishAffinity();
                    startActivity(getActivity().getIntent());
                })
                .build()
                .show(getChildFragmentManager(), DialogSound.class.getName()));

        initAds();
    }

    private void showDialogSetTime() {
        new DialogSelectItem.ExtendBuilder()
                .setLstData(Config.lstTimer)
                .setIdDefault(PreferencesHelper.getInt(PreferencesHelper.SETTING_VALUE_TIMER
                        , Config.DEFAULT_TIMER))
                .setTitle(getString(R.string.set_timer))
                .onSetPositiveButton(getString(R.string.save), (baseDialog, datas) -> {
                    int idSelected = (int) datas.get(DialogSelectItem.ITEM_SAVE);
                    PreferencesHelper.putInt(PreferencesHelper.SETTING_VALUE_TIMER, idSelected);
                    baseDialog.dismiss();
                })
                .build()
                .show(getChildFragmentManager(), DialogSelectItem.class.getName());
    }

    private void showDialogSound() {
        new DialogSound.ExtendBuilder()
                .setTitle(getString(R.string.sound))
                .onSetPositiveButton(getString(R.string.save), (baseDialog, datas) -> {
                    int value = (int) datas.get(DialogSound.SET_SOUND);
                    PreferencesHelper.putInt(PreferencesHelper.SETTING_VALUE_SOUND, value);
                    baseDialog.dismiss();
                })
                .build()
                .show(getChildFragmentManager(), DialogSound.class.getName());
    }

    private void showDialogAlarmTone() {
        SelectModel[] lstData = new SelectModel[Config.LIST_TONE.length];
        for (int i = 0; i < Config.LIST_TONE.length; i++) {
            lstData[i] = new SelectModel(i, getString(R.string.tone, i + 1));
        }
        final MediaPlayer[] player = {null};
        new DialogSelectItem.ExtendBuilder()
                .setLstData(lstData)
                .setIdDefault(PreferencesHelper.getInt(PreferencesHelper.SETTING_VALUE_TONE, Config.DEFAULT_TONE))
                .setItemClickListener(position -> {
                    if (player[0] != null)
                        player[0].release();
                    player[0] = MediaPlayer.create(getContext(), Config.LIST_TONE[position]);
                    player[0].start();
                })
                .setTitle(getString(R.string.alarm_tone))
                .onSetPositiveButton(getString(R.string.save), (baseDialog, datas) -> {
                    int idSelected = (int) datas.get(DialogSelectItem.ITEM_SAVE);
                    PreferencesHelper.putInt(PreferencesHelper.SETTING_VALUE_TONE, idSelected);
                    baseDialog.dismiss();
                })
                .onDismissListener(() -> {
                    if (player[0] != null)
                        player[0].release();
                })
                .build()
                .show(getChildFragmentManager(), DialogSelectItem.class.getName());
    }

    @Override
    protected FragmentSettingBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSettingBinding.inflate(inflater, container, false);
    }

    @Override
    protected int getTitleFragment() {
        return R.string.setting;
    }

    private void initAds() {
      AdsUtils.nativeSettingLoadFail.observe(this, new Observer<Boolean>() {
          @Override
          public void onChanged(Boolean aBoolean) {
              if(aBoolean) {
                  binding.frAds.setVisibility(View.GONE);
              }
          }
      });

      AdsUtils.nativeSetting.observe(this, new Observer<ApNativeAd>() {
          @Override
          public void onChanged(ApNativeAd apNativeAd) {
              VioAdmob.getInstance().populateNativeAdView(
                      requireActivity(),
                      apNativeAd,
                      binding.frAds,
                      binding.includeNative.shimmerContainerBanner
              );
          }
      });
    }
}