package com.emopass.antitheftalarm.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.databinding.LayoutHeaderLockviewBinding;
import com.emopass.antitheftalarm.databinding.LayoutPinCodeBinding;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.Toolbox;
import com.emopass.antitheftalarm.utils.ViewUtils;

public class PinCodeLayout extends ConstraintLayout {
    private LayoutPinCodeBinding binding;
    private LayoutHeaderLockviewBinding headerLockviewBinding;
    private CheckPasswordCodeListener listener;
    private Context context;

    public void setListener(CheckPasswordCodeListener listener) {
        this.listener = listener;
    }

    public PinCodeLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PinCodeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        binding = LayoutPinCodeBinding.inflate(LayoutInflater.from(context), this, true);
        headerLockviewBinding = LayoutHeaderLockviewBinding.bind(binding.llHeaderLockview.getRoot());
        binding.btn0.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.ZERO));
        binding.btn1.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.ONE));
        binding.btn2.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.TWO));
        binding.btn3.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.THREE));
        binding.btn4.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.FOUR));
        binding.btn5.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.FIVE));
        binding.btn6.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.SIX));
        binding.btn7.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.SEVEN));
        binding.btn8.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.EIGHT));
        binding.btn9.setOnClickListener(v -> binding.pinView.append(Config.TextPassword.NINE));
        binding.btnX.setOnClickListener(v -> binding.pinView.setText(Config.TextPassword.EMPTY));
        binding.pinView.setOnCheckPinCode(pin -> {
            if (pin.equals(PreferencesHelper.getPinCode())) {
                listener.onCheck(CheckPasswordCodeListener.State.SUCCESS, pin);
            } else {
                binding.pinView.onPinCodeFailed();
                binding.pinView.setText(Config.TextPassword.EMPTY);
                listener.onCheck(CheckPasswordCodeListener.State.FAILED, pin);
                YoYo.with(Techniques.Shake)
                        .duration(700)
                        .playOn(headerLockviewBinding.tvMessage);
            }
            binding.pinView.setText(Config.TextPassword.EMPTY);
        });

//        headerLockviewBinding.imMenu.setVisibility(VISIBLE);
        headerLockviewBinding.imMenu.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), headerLockviewBinding.imMenu);
            popupMenu.getMenuInflater().inflate(R.menu.menu_lockview, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                return true;
            });
            popupMenu.show();
        });
    }

    public void cleanPinCode() {
        binding.pinView.setText(Config.TextPassword.EMPTY);
    }

    public void setInforApplication(String packageName) {
        try {
            headerLockviewBinding.imvIcon.setImageDrawable(Toolbox.getdIconApplication(getContext(), packageName));
            headerLockviewBinding.tvMessage.setText(Toolbox.getdNameApplication(getContext(), packageName));

            Drawable iconApp = Toolbox.getdIconApplication(getContext(), packageName);
            binding.imBackground.setBackgroundDrawable(iconApp);
            binding.imBackground.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            binding.imBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                            binding.imBackground.buildDrawingCache();
                            int width = binding.imBackground.getWidth(), height = binding.imBackground.getHeight();
                            if (width != 0 || height != 0) {
                                Bitmap bmp = ViewUtils.drawableToBitmap(iconApp, width, height);
                                ViewUtils.blur(context, ViewUtils.big(bmp), binding.imBackground, width, height);
                            } else {
                                binding.llHeader.setBackgroundColor(context.getResources().getColor(R.color.color_111111));
                                binding.llContent.setBackgroundColor(context.getResources().getColor(R.color.white));
                            }
                            return true;
                        }
                    });
        } catch (Exception e) {

        }
    }

}

