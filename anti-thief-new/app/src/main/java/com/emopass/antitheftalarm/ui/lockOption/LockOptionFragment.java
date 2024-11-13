package com.emopass.antitheftalarm.ui.lockOption;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.emopass.antitheftalarm.App;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.FragmentLockOptionBinding;
import com.emopass.antitheftalarm.ui.BaseFragment;
import com.emopass.antitheftalarm.ui.password.PasswordActivity;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;

public class LockOptionFragment extends BaseFragment<FragmentLockOptionBinding> {
    private static final int REQUEST_SWITCH_TO_PIN_CODE = 511;
    private static final int REQUEST_SWITCH_TO_PATERN_CODE = 512;

    @Override
    protected void initView() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isSelfie = PreferencesHelper.getBoolean(PreferencesHelper.INTRUDER_SELFIE, false);
                binding.funcIntruderSelfie.setSwChecked(isSelfie);
                boolean isHidePattern = PreferencesHelper.getBoolean(PreferencesHelper.HIDE_PATTERN, false);
                binding.funcHidePattern.setSwChecked(isHidePattern);
                boolean isFingerprint = PreferencesHelper.getBoolean(PreferencesHelper.FINGERPRINT_UNLOCK, false);
                binding.funcFingerprint.setSwChecked(isFingerprint);
            }
        }, 50);
        binding.funcSwitchPin.setTitle(PreferencesHelper.isPatternCode()
                ? getResources().getString(R.string.switch_to_pin)
                : getResources().getString(R.string.switch_to_patern));
    }

    @Override
    protected void initControl() {
        binding.funcIntruderSelfie.setItemClickListener(() -> {
            if (PreferencesHelper.getBoolean(PreferencesHelper.INTRUDER_SELFIE, false)) {
                navigate(R.id.action_lock_option_to_intruder);
            }
        });
        binding.funcSwitchPin.setItemClickListener(() -> {
            App.getInstace().setForceLockScreen(true);
            if (PreferencesHelper.isPatternCode()) {
                gotoCheckPatternCode();
            } else {
                gotoCheckPinCode();
            }
        });
        binding.funcResetPass.setItemClickListener(() -> {
           changePassword();
        });
        binding.funcHidePattern.setActionListener(isChecked -> {
            PreferencesHelper.putBoolean(PreferencesHelper.HIDE_PATTERN, isChecked);
        });
        binding.funcFingerprint.setActionListener(isChecked -> {
            PreferencesHelper.putBoolean(PreferencesHelper.FINGERPRINT_UNLOCK, isChecked);
        });
        binding.funcIntruderSelfie.setActionListener(isChecked -> {
            if (isChecked) {
                binding.funcIntruderSelfie.setSwChecked(false);
                getBaseActivity().askPermissionStorage(() -> {
                    getBaseActivity().askPermissionCamera(() -> {
                        binding.funcIntruderSelfie.setSwChecked(true);
                        PreferencesHelper.putBoolean(PreferencesHelper.INTRUDER_SELFIE, true);
                        return null;
                    });
                    return null;
                });
            } else {
                PreferencesHelper.putBoolean(PreferencesHelper.INTRUDER_SELFIE, false);
            }
        });
    }

    private void changePassword() {
        App.getInstace().setForceLockScreen(true);
        Intent intent = new Intent(getActivity(), PasswordActivity.class);
        if (PreferencesHelper.isPatternCode())
            intent.setAction(Config.ActionIntent.ACTION_CHANGE_PATTERN_CODE);
        else
            intent.setAction(Config.ActionIntent.ACTION_CHANGE_PIN_CODE);
        startActivity(intent);
    }

    private void gotoCheckPinCode() {
        Intent intent = new Intent(getActivity(), PasswordActivity.class);
        intent.setAction(Config.ActionIntent.ACTION_SWITCH_TO_PATTERN_CODE);
        startActivityForResult(intent, REQUEST_SWITCH_TO_PATERN_CODE);
    }

    private void gotoCheckPatternCode() {
        Intent intent = new Intent(getActivity(), PasswordActivity.class);
        intent.setAction(Config.ActionIntent.ACTION_SWITCH_TO_PIN_CODE);
        startActivityForResult(intent, REQUEST_SWITCH_TO_PIN_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SWITCH_TO_PATERN_CODE:
                case REQUEST_SWITCH_TO_PIN_CODE:
                    binding.funcSwitchPin.setTitle(PreferencesHelper.isPatternCode()
                            ? getResources().getString(R.string.switch_to_pin)
                            : getResources().getString(R.string.switch_to_patern));
                    break;
            }
        }
    }

    @Override
    protected FragmentLockOptionBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentLockOptionBinding.inflate(inflater, container, false);
    }

    @Override
    protected int getTitleFragment() {
        return R.string.lock_option;
    }
}
