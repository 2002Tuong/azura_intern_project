package com.emopass.antitheftalarm.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewbinding.ViewBinding;

import com.emopass.antitheftalarm.App;
import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.dialog.DialogAskPermission;
import com.emopass.antitheftalarm.ui.guildPermission.GuildPermissionActivity;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.SystemUtil;
import com.emopass.antitheftalarm.utils.Toolbox;

import java.util.concurrent.Callable;

public abstract class BaseActivity<B extends ViewBinding> extends AppCompatActivity {

    private Callable<Void> callable;

    protected B binding;

    public static final int REQUEST_ACCESSIBILITY_SETTINGS = 1111;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SystemUtil.setLocale(this, SystemUtil.getPreLanguage(this));
        binding = getViewBinding();
        setContentView(binding.getRoot());
        App.getInstace().doForCreate(this);
        View layoutPadding = getViewPadding();
        if (layoutPadding != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Toolbox.getHeightStatusBar(this) > 0) {
                layoutPadding.setPadding(0, Toolbox.getHeightStatusBar(this), 0, 0);
            }
        }
        Toolbox.setStatusBarHomeWhite(this);
        initView();
        initControl();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getInstace().doForFinish(this);
    }

    public void askPermissionCamera(Callable<Void> callable) {
        this.callable = callable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                new DialogAskPermission.ExtendBuilder()
                        .setPermissionName(Manifest.permission.CAMERA)
                        .onSetPositiveButton(getString(R.string.grant_now), (baseDialog, datas) -> {
                            App.getInstace().setForceLockScreen(true);
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.CAMERA}, Config.MY_PERMISSIONS_REQUEST_CAMERA);
                            baseDialog.dismiss();
                        })
                        .build()
                        .show(getSupportFragmentManager(), DialogAskPermission.class.getName());
            } else {
                try {
                    callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void askPermissionStorage(Callable<Void> callable) {
        this.callable = callable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                new DialogAskPermission.ExtendBuilder()
                        .setPermissionName(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .onSetPositiveButton(getString(R.string.grant_now), (baseDialog, datas) -> {
                            App.getInstace().setForceLockScreen(true);
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                            , Manifest.permission.WRITE_EXTERNAL_STORAGE}, Config.MY_PERMISSIONS_REQUEST_STORAGE);
                            baseDialog.dismiss();
                        })
                        .build()
                        .show(getSupportFragmentManager(), DialogAskPermission.class.getName());
            } else {
                try {
                    callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void requestDetectHome() {
        new DialogAskPermission.ExtendBuilder()
                .setPermissionName(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .onSetPositiveButton(getString(R.string.grant_now), (baseDialog, datas) -> {
                    App.getInstace().setForceLockScreen(true);
                    startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),REQUEST_ACCESSIBILITY_SETTINGS);
                    GuildPermissionActivity.openActivityGuildPermission(this, Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    baseDialog.dismiss();
                })
                .build()
                .show(getSupportFragmentManager(), DialogAskPermission.class.getName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Config.MY_PERMISSIONS_REQUEST_CAMERA:
            case Config.MY_PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    callListener();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Config.PERMISSIONS_DRAW_APPICATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this))
                    callListener();
                break;
            case Config.PERMISSIONS_USAGE:
                if (SystemUtil.isUsageAccessAllowed(this)) {
                    callListener();
                }
                break;
        }
    }

    private void callListener() {
        try {
            this.callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected View getViewPadding() {
        return null;
    }

    protected abstract void initView();

    protected abstract B getViewBinding();

    protected abstract void initControl();

    protected TextView getToolbarTitle() {
        return null;
    }

    protected void setTitleToolbar(String title) {
        if (!TextUtils.isEmpty(title) && getToolbarTitle() != null)
            getToolbarTitle().setText(title);
    }

    public String getTitleString() {
        return getToolbarTitle().getText().toString();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    public void toast(String content) {
        if (!TextUtils.isEmpty(content))
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    public void navigate(int id) {
        Navigation.findNavController(binding.getRoot()).navigate(
                id,
                null,
                null,
                null);
    }

    public void checkdrawPermission(Callable<Void> callable) {
        this.callable = callable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new DialogAskPermission.ExtendBuilder()
                    .setPermissionName(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    .onSetPositiveButton(getString(R.string.grant_now), (baseDialog, datas) -> {
                        App.getInstace().setForceLockScreen(true);
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, Config.PERMISSIONS_DRAW_APPICATION);
                        //GuildPermissionActivity.openActivityGuildPermission(this, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        baseDialog.dismiss();

                        // Monitor to not show ads when coming back from the permission settings
                        PreferencesHelper.setCheckingPermission(true);
                    })
                    .build()
                    .show(getSupportFragmentManager(), DialogAskPermission.class.getName());
        } else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void askPermissionUsageSetting(Callable<Void> callable) {
        this.callable = callable;
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && !SystemUtil.isUsageAccessAllowed(this)) {
            new DialogAskPermission.ExtendBuilder()
                    .setPermissionName(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .onSetPositiveButton(getString(R.string.grant_now), (baseDialog, datas) -> {
                        App.getInstace().setForceLockScreen(true);
                        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), Config.PERMISSIONS_USAGE);
                        GuildPermissionActivity.openActivityGuildPermission(this, Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        baseDialog.dismiss();
                    })
                    .build()
                    .show(getSupportFragmentManager(), DialogAskPermission.class.getName());

        } else {
            try {
                callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
