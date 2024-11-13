package com.emopass.antitheftalarm.ui.dectection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import com.ads.control.ads.VioAdmob;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.FragmentDetectionBinding;
import com.emopass.antitheftalarm.service.AntiTheftService;
import com.emopass.antitheftalarm.service.BackgroundManager;
import com.emopass.antitheftalarm.ui.BaseFragment;
import com.emopass.antitheftalarm.utils.AdsUtils;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.CounDownTimer;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.SystemUtil;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.concurrent.TimeUnit;

public class DetectionFragment extends BaseFragment<FragmentDetectionBinding> {

  private int typeDetection;
  private boolean isRunCountDown;
  private String detectionName = "";

  private FirebaseAnalytics firebaseAnalytics;

  @Override
  protected void initView() {
    AdsUtils.getInstance().loadInterFunction(requireContext(), false);
    if (getArguments() != null) {
      typeDetection = getArguments().getInt(Config.TYPE_DETECTION);
    }
    if (typeDetection == Config.DETECTION_PROXIMITY) {
      setTitleToolbar(getString(R.string.proximily_detection));
      detectionName = "Proximity";
    } else if (typeDetection == Config.DETECTION_MOTION) {
      setTitleToolbar(getString(R.string.motion_detection));
      detectionName = "Motion";
    } else if (typeDetection == Config.DETECTION_CHARGER) {
      setTitleToolbar(getString(R.string.charger_detection));
      detectionName = "Charger";
    } else if (typeDetection == Config.DETECTION_HANDSFREE) {
      setTitleToolbar(getString(R.string.hands_detection));
      detectionName = "Handsfree";
    } else if (typeDetection == Config.DETECTION_WIFI) {
      setTitleToolbar(getString(R.string.wifi_detection));
      detectionName = "Wi-Fi";
    } else if (typeDetection == Config.DETECTION_FULL_BATTERY) {
      setTitleToolbar(getString(R.string.full_battery_detection));
      detectionName = "Full Battery";
    }

    binding.rippleBackground.startRippleAnimation();

    // Analytics
    firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

    Bundle bundle = new Bundle();
    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "open_detection_screen - " + detectionName);
    bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.getClass().getSimpleName() + " - " + detectionName);
    bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.getClass().getSimpleName() + " - " + detectionName);
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    AdsUtils.getInstance().requestNativeFunction(requireActivity(), false);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateDetection();
  }

  @Override
  public void onDestroyView() {
    AdsUtils.getInstance().forceShowInterFunction(requireContext(), () -> {
    });
    super.onDestroyView();
  }

  private void updateDetection() {
    if (PreferencesHelper.getInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE)
        == typeDetection) {
      onFinishTick();
    } else {
      onChangeToNormal();
    }
  }

  private void countDownTime() {
    int time =
        PreferencesHelper.getInt(PreferencesHelper.SETTING_VALUE_TIMER, Config.DEFAULT_TIMER);
    compositeDisposable.add(new CounDownTimer(time, 1, TimeUnit.SECONDS) {
      @Override
      public void onTick(int tickValue) {
        onTickValue(tickValue);
        isRunCountDown = true;
      }

      @Override
      public void onFinish() {
        toast(getResources().getString(R.string.activate_alarm));
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        onFinishTick();
        startDetection();
        isRunCountDown = false;

        //Send analytics event
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Activate mode - " + detectionName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.getClass().getSimpleName() + " - " + detectionName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.getClass().getSimpleName() + " - " + detectionName);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
      }
    }.start().getDisposable());
  }

  private void onTickValue(int tickValue) {
    binding.tvTimeCoundown.setText(tickValue + "");
    binding.tvTimeCoundown.setVisibility(View.VISIBLE);

    binding.tvActive.setText("");
    binding.tvActive.setBackgroundResource(R.drawable.btn_round_primary_coundown);

    YoYo.with(Techniques.ZoomIn).duration(600).playOn(binding.tvTimeCoundown);
  }

  private void onChangeToNormal() {
    binding.tvTimeCoundown.setVisibility(View.GONE);
    binding.tvActive.setText(getResources().getString(R.string.activate));
    binding.tvActive.setBackgroundResource(R.drawable.btn_round_primary);
  }

  private void onFinishTick() {
    binding.tvTimeCoundown.setVisibility(View.GONE);
    binding.tvActive.setText(getResources().getString(R.string.activate_alarm));
    binding.tvActive.setBackgroundResource(R.drawable.btn_round_primary_coundown);
  }

  private void startDetection() {
    Intent intent = new Intent(getContext(), AntiTheftService.class);
    if (typeDetection == Config.DETECTION_PROXIMITY) {
      intent.setAction(Config.ActionIntent.ACTION_START_DETECTION_PROXIMITY);
    }
    if (typeDetection == Config.DETECTION_MOTION) {
      intent.setAction(Config.ActionIntent.ACTION_START_DETECTION_MOTION);
    }
    if (typeDetection == Config.DETECTION_CHARGER) {
      if (!SystemUtil.isCharging(getActivity())) {
        onChangeToNormal();
        return;
      } else {
        PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_CHARGER);
      }
    }
    if (typeDetection == Config.DETECTION_HANDSFREE) {
      intent.setAction(Config.ActionIntent.ACTION_START_DETECTION_HANDSFREE);
      if (!SystemUtil.isHeadsetPlugged(getActivity())) {
        onChangeToNormal();
        return;
      } else {
        PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_HANDSFREE);
      }
    }
    if (typeDetection == Config.DETECTION_WIFI) {
      intent.setAction(Config.ActionIntent.ACTION_START_DETECTION_WIFI);
      if (!SystemUtil.isWiFiConnected(getActivity())) {
        onChangeToNormal();
        return;
      } else {
        PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_WIFI);
      }
    }
    if (typeDetection == Config.DETECTION_FULL_BATTERY) {
      intent.setAction(Config.ActionIntent.ACTION_START_DETECTION_FULL_BATTERY);
    }

    getActivity().startService(intent);
  }

  private void stopDetection() {
    if (!BackgroundManager.getInstance(getActivity()).canStartService()) return;
    Intent intent = new Intent(getContext(), AntiTheftService.class);
    if (typeDetection == Config.DETECTION_PROXIMITY) {
      intent.setAction(Config.ActionIntent.ACTION_STOP_DETECTION_PROXIMITY);
    }
    if (typeDetection == Config.DETECTION_MOTION) {
      intent.setAction(Config.ActionIntent.ACTION_STOP_DETECTION_MOTION);
    }
    if (typeDetection == Config.DETECTION_CHARGER) {
      intent.setAction(Config.ActionIntent.ACTION_STOP_DETECTION_CHARGER);
    }

    if (typeDetection == Config.DETECTION_HANDSFREE) {
      intent.setAction(Config.ActionIntent.ACTION_STOP_DETECTION_HANDSFREE);
    }
    if (typeDetection == Config.DETECTION_WIFI) {
      intent.setAction(Config.ActionIntent.ACTION_STOP_DETECTION_WIFI);
    }
    if (typeDetection == Config.DETECTION_FULL_BATTERY) {
      intent.setAction(Config.ActionIntent.ACTION_STOP_DETECTION_FULL_BATTERY);
    }
    getActivity().startService(intent);
  }

  @Override
  protected void initControl() {
    binding.tvActive.setOnClickListener(v -> {
      getBaseActivity().checkdrawPermission(() -> {
        onActiveDetection();
        return null;
      });
    });

    initAds();
  }

  private void onActiveDetection() {
    int typeAction =
        PreferencesHelper.getInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE);
    if (typeAction != Config.DETECTION_NONE && typeAction != typeDetection) {
        if (typeAction == Config.DETECTION_MOTION) {
            toast(getResources().getString(R.string.message_motion_actived));
        }
        if (typeAction == Config.DETECTION_PROXIMITY) {
            toast(getResources().getString(R.string.message_proximity_actived));
        }
        if (typeAction == Config.DETECTION_CHARGER) {
            toast(getResources().getString(R.string.message_charger_actived));
        }
        if (typeAction == Config.DETECTION_HANDSFREE) {
            toast(getResources().getString(R.string.message_handsfree_actived));
        }
        if (typeAction == Config.DETECTION_WIFI) {
            toast(getResources().getString(R.string.message_wifi_actived));
        }
        if (typeAction == Config.DETECTION_FULL_BATTERY) {
            toast(getResources().getString(R.string.message_full_battery_actived));
        }
      return;
    }
    if (isRunCountDown || typeAction != Config.DETECTION_NONE) {
      compositeDisposable.clear();
      stopDetection();
      onChangeToNormal();
      isRunCountDown = false;
    } else {
      if (typeDetection == Config.DETECTION_CHARGER
          || typeDetection == Config.DETECTION_FULL_BATTERY) {
        if (SystemUtil.isCharging(getContext())) {
          countDownTime();
        } else {
          toast(getResources().getString(R.string.message_plug_in_before_using));
        }
      } else if (typeDetection == Config.DETECTION_HANDSFREE) {
        if (SystemUtil.isHeadsetPlugged(getContext())) {
          countDownTime();
        } else {
          toast(getResources().getString(R.string.message_plug_headset_before_using));
        }
      }else if (typeDetection == Config.DETECTION_WIFI) {
        if (SystemUtil.isWiFiConnected(getContext())) {
          countDownTime();
        } else {
          toast(getResources().getString(R.string.message_connect_wifi_before_using));
        }
      } else {
        countDownTime();
      }
    }
  }

  @Override
  protected FragmentDetectionBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
    return FragmentDetectionBinding.inflate(inflater, container, false);
  }

  @Override
  protected int getTitleFragment() {
    return R.string.proximily_detection;
  }

  private void initAds() {
    AdsUtils.nativeFunctionLoadFail.observe(this, new Observer<Boolean>() {
      @Override
      public void onChanged(Boolean aBoolean) {
        if(aBoolean) {
          binding.frAds.setVisibility(View.GONE);
        }
      }
    });

    AdsUtils.nativeFunction.observe(this, new Observer<ApNativeAd>() {
      @Override
      public void onChanged(ApNativeAd apNativeAd) {
        if(apNativeAd != null) {
          VioAdmob.getInstance().populateNativeAdView(
                  requireActivity(),
                  apNativeAd,
                  binding.frAds,
                  binding.includeNative.shimmerContainerBanner
          );
        }
      }
    });
  }
}