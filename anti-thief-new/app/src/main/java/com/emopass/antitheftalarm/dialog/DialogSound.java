package com.emopass.antitheftalarm.dialog;

import android.view.LayoutInflater;
import android.widget.TextView;

import com.emopass.antitheftalarm.databinding.DialogSoundBinding;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;

import java.util.HashMap;

public class DialogSound extends BaseDialog<DialogSoundBinding, DialogSound.ExtendBuilder> {

    public static String SET_SOUND = "set sound";

    public DialogSound(DialogSound.ExtendBuilder builder) {
        super(builder);
    }

    @Override
    protected DialogSoundBinding getViewBinding() {
        return DialogSoundBinding.inflate(LayoutInflater.from(getContext()));
    }

    @Override
    protected void initView() {
        super.initView();
        int soundValue = PreferencesHelper.getInt(PreferencesHelper.SETTING_VALUE_SOUND, Config.DEFAULT_SOUND);
        binding.seekbarSound.setProgress(soundValue);
    }

    @Override
    protected void initControl() {
        binding.imClose.setOnClickListener(view -> dismiss());
    }

    @Override
    protected TextView getTitle() {
        return binding.tvTitle;
    }

    @Override
    protected TextView getPositiveButton() {
        return binding.tvPositive;
    }

    @Override
    protected void handleClickPositiveButton(HashMap<String, Object> datas) {
        datas.put(SET_SOUND, binding.seekbarSound.getProgress());
        super.handleClickPositiveButton(datas);
    }

    public static class ExtendBuilder extends BuilderDialog {

        @Override
        public BaseDialog build() {
            return new DialogSound(this);
        }
    }
}
