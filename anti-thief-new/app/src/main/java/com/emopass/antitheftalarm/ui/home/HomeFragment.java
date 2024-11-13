package com.emopass.antitheftalarm.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import com.ads.control.ads.VioAdmob;
import com.ads.control.ads.wrapper.ApNativeAd;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.FragmentHomeBinding;
import com.emopass.antitheftalarm.ui.BaseFragment;
import com.emopass.antitheftalarm.utils.AdsUtils;
import com.emopass.antitheftalarm.utils.Config;

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    @Override
    protected void initView() {
        setStageDrawerLayout(false);
        setTitleToolbar(getString(R.string.app_name));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AdsUtils.getInstance().requestNativeHome(requireActivity(),false);
        AdsUtils.getInstance().initBanner(requireActivity(), view);
    }

    @Override
    protected void initControl() {
        binding.funcProximityDetection.setItemClickListener(() -> {
            //AdmobHelp.getInstance().showInterstitialAd(() ->
            //    getBaseActivity().checkdrawPermission(() -> {
                Bundle bundle = new Bundle();
                bundle.putInt(Config.TYPE_DETECTION, Config.DETECTION_PROXIMITY);
                navigate(R.id.action_home_to_detection, bundle);
            //    return null;
            //}));
        });
        binding.funcMotionDetection.setItemClickListener(() -> {
            //AdmobHelp.getInstance().showInterstitialAd(() -> getBaseActivity().checkdrawPermission(() -> {
                Bundle bundle = new Bundle();
                bundle.putInt(Config.TYPE_DETECTION, Config.DETECTION_MOTION);
                navigate(R.id.action_home_to_detection, bundle);
                //return null;
            //}));
        });
        binding.funcChargerDetection.setItemClickListener(() -> {
            //AdmobHelp.getInstance().showInterstitialAd(() -> getBaseActivity().checkdrawPermission(() -> {
                Bundle bundle = new Bundle();
                bundle.putInt(Config.TYPE_DETECTION, Config.DETECTION_CHARGER);
                navigate(R.id.action_home_to_detection, bundle);
            //    return null;
            //}));
        });

        binding.funcHandsfreeDetection.setItemClickListener(() -> {
            //AdmobHelp.getInstance().showInterstitialAd(() -> getBaseActivity().checkdrawPermission(() -> {
                Bundle bundle = new Bundle();
                bundle.putInt(Config.TYPE_DETECTION, Config.DETECTION_HANDSFREE);
                navigate(R.id.action_home_to_detection, bundle);
            //    return null;
            //}));
        });

        binding.funcWifiDetection.setItemClickListener(() -> {
            //AdmobHelp.getInstance().showInterstitialAd(() -> getBaseActivity().checkdrawPermission(() -> {
                Bundle bundle = new Bundle();
                bundle.putInt(Config.TYPE_DETECTION, Config.DETECTION_WIFI);
                navigate(R.id.action_home_to_detection, bundle);
                //return null;
            //}));
        });

        binding.funcFullBatteryDetection.setItemClickListener(() -> {
            //AdmobHelp.getInstance().showInterstitialAd(() -> getBaseActivity().checkdrawPermission(() -> {
                Bundle bundle = new Bundle();
                bundle.putInt(Config.TYPE_DETECTION, Config.DETECTION_FULL_BATTERY);
                navigate(R.id.action_home_to_detection, bundle);
            //    return null;
            //}));
        });

        initAds();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setStageDrawerLayout(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater mMenuInflater) {
        mMenuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                navigate(R.id.action_home_to_setting);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected FragmentHomeBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected int getTitleFragment() {
        return R.string.app_name;
    }

    private void initAds() {
        AdsUtils.nativeHomeLoadFail.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    binding.frAds.setVisibility(View.GONE);
                }
            }
        });

        AdsUtils.nativeHome.observe(this, new Observer<ApNativeAd>() {
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