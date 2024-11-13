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
import com.emopass.antitheftalarm.databinding.LayoutPatternCodeBinding;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.Toolbox;
import com.emopass.antitheftalarm.utils.ViewUtils;

public class PatternLockLayout extends ConstraintLayout {
    private LayoutPatternCodeBinding binding;
    private LayoutHeaderLockviewBinding headerLockviewBinding;
    private CheckPasswordCodeListener listener;
    private Context context;

    public void setListener(CheckPasswordCodeListener listener) {
        this.listener = listener;
    }

    public PatternLockLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PatternLockLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        binding = LayoutPatternCodeBinding.inflate(LayoutInflater.from(context), this, true);
        headerLockviewBinding = LayoutHeaderLockviewBinding.bind(binding.llHeaderLockview.getRoot());
        binding.patternCode.setFinishInterruptable(false);
        binding.patternCode.setCallBack(password -> {
            if (password.string.equals(PreferencesHelper.getPatternCode())) {
                return checkPatternCodeSuccess(password.string);
            } else {
                return checkPatternCodeFailed(password.string);
            }
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

    private int checkPatternCodeSuccess(String passInput) {
        if (listener != null)
            listener.onCheck(CheckPasswordCodeListener.State.SUCCESS, passInput);
        return PatternLockView.CODE_PASSWORD_ERROR;
    }

    private int checkPatternCodeFailed(String passInput) {
        YoYo.with(Techniques.Shake)
                .duration(700)
                .playOn(headerLockviewBinding.tvMessage);
        if (listener != null)
            listener.onCheck(CheckPasswordCodeListener.State.FAILED, passInput);
        return PatternLockView.CODE_PASSWORD_ERROR;
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

