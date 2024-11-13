package com.emopass.antitheftalarm.widget;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.emopass.antitheftalarm.databinding.LayoutLockviewManagerBinding;
import com.emopass.antitheftalarm.utils.Camera2Controller;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.FingerprintManager;
import com.emopass.antitheftalarm.utils.PreferencesHelper;

public class LockViewWindowManager extends ConstraintLayout implements CheckPasswordCodeListener {

    private LayoutLockviewManagerBinding binding;
    private Context context;
    private PasswordConfirmListener passwordConfirmListener;
    private FingerprintManager fingerprintManager;
    private Camera2Controller camera2Controller;
    private int countEntries = 0;

    public interface PasswordConfirmListener {
        void onSuccess();

        void onFails(String passInput);
    }

    private void initFingerprint() {
        if (!PreferencesHelper.getBoolean(PreferencesHelper.FINGERPRINT_UNLOCK, false))
            return;
        fingerprintManager = new FingerprintManager(context, new FingerprintManager.FingerListener() {
            @Override
            public void onSuccessListener() {
                if (passwordConfirmListener != null) {
                    passwordConfirmListener.onSuccess();
                }
            }

            @Override
            public void onFailsListener() {

            }
        });
        fingerprintManager.initFinger();
    }

    public void setPasswordConfirmListener(PasswordConfirmListener passwordConfirmListener) {
        this.passwordConfirmListener = passwordConfirmListener;
    }

    public LockViewWindowManager(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    private void initView() {
        binding = LayoutLockviewManagerBinding.inflate(LayoutInflater.from(context), this, true);
        setVisibility(GONE);
        initListener();
    }

    private void initListener() {
        binding.patternLockLayout.setListener(this);
        binding.pinCodeLayout.setListener(this);
    }

    public void showhideViewPassword(boolean isShow, String packageName) {
        if (!isShow) {
            if (camera2Controller != null) {
                camera2Controller.releaseCamera();
                camera2Controller = null;
            }
            setVisibility(GONE);
            binding.patternLockLayout.setVisibility(GONE);
            binding.pinCodeLayout.setVisibility(GONE);
            binding.pinCodeLayout.cleanPinCode();
        } else {
            if (camera2Controller == null) {
                camera2Controller = new Camera2Controller(binding.textureview, getContext());

            }
            setVisibility(VISIBLE);
            if (PreferencesHelper.isPatternCode()) {
                binding.patternLockLayout.setVisibility(VISIBLE);
                binding.patternLockLayout.setInforApplication(packageName);
            } else {
                binding.pinCodeLayout.setVisibility(VISIBLE);
                binding.pinCodeLayout.setInforApplication(packageName);
            }
            initFingerprint();
        }
    }

    @Override
    public void onCheck(State state, String passInput) {
        if (passwordConfirmListener != null) {
            if (state == State.FAILED) {
                passwordConfirmListener.onFails(passInput);
                captureWhenFailed(passInput);
            } else {
                passwordConfirmListener.onSuccess();
            }
        }
    }

    private void captureWhenFailed(String pin) {
        int entries = PreferencesHelper.getInt(PreferencesHelper.INTRUDER_SELFIE_ENTRIES, Config.DEFAULT_INTRUDER_SELFIE);
        countEntries++;
        if (countEntries >= entries && camera2Controller != null) {
            camera2Controller.takePicture(getContext(), pin);
            countEntries = 0;
        }
    }

}
