package com.emopass.antitheftalarm.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.emopass.antitheftalarm.sensor.DeviceSensorManager;
import com.emopass.antitheftalarm.service.notification.ServiceNotificationManager;
import com.emopass.antitheftalarm.service.receiver.BatteryStatusReceiver;
import com.emopass.antitheftalarm.service.receiver.HandFreeReceiver;
import com.emopass.antitheftalarm.service.receiver.ScreenReceiver;
import com.emopass.antitheftalarm.service.receiver.WatchmenReceiver;
import com.emopass.antitheftalarm.service.receiver.WiFiChangeReceiver;
import com.emopass.antitheftalarm.ui.main.MainActivity;
import com.emopass.antitheftalarm.ui.password.PasswordActivity;
import com.emopass.antitheftalarm.utils.Config;
import com.emopass.antitheftalarm.utils.PreferencesHelper;
import com.emopass.antitheftalarm.utils.Toolbox;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AntiTheftService extends Service implements ServiceNotificationManager.ClickNotificationListener, DeviceSensorManager.CallbackSensor {

    private BatteryStatusReceiver batteryStatusReceiver;
    private ScreenReceiver screenOnOffReceiver;
    private HandFreeReceiver handFreeReceiver;
    private WiFiChangeReceiver wiFiChangeReceiver;
    private LockManager lockManager;
    private DeviceSensorManager deviceSensorManager;
    private AlarmDuringAntiTheftManager alarmDuringAntiTheftManager;
    private Runnable runnable;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeAppLockerNotification();
        initLockManager();
        initAlarmDuringAntiTheft();
        registerBroastcastReceiver();
        handler = new Handler();
        runLockApp();
    }

    private void initAlarmDuringAntiTheft() {
        if (alarmDuringAntiTheftManager == null) {
            alarmDuringAntiTheftManager = new AlarmDuringAntiTheftManager(this);
        }
    }

    private void initLockManager() {
        lockManager = new LockManager(this);
    }

    private void registerBroastcastReceiver() {
        /** Lẳng nghe các sự kiện bảo vệ điện thoại*/
        if (deviceSensorManager == null) {
            deviceSensorManager = new DeviceSensorManager(this, this::onDetect);
        }
        /** Sự kiện thay đổi trạng thái sạc*/
        if (batteryStatusReceiver == null) {
            batteryStatusReceiver = new BatteryStatusReceiver();
            batteryStatusReceiver.onCreate(this);
        }

        if (handFreeReceiver == null) {
            handFreeReceiver = new HandFreeReceiver();
            handFreeReceiver.onCreate(this);
        }

        if (wiFiChangeReceiver == null) {
            wiFiChangeReceiver = new WiFiChangeReceiver();
            wiFiChangeReceiver.onCreate(this);
        }

        /** Sự kiện on off màn hình*/
        if (screenOnOffReceiver == null) {
            screenOnOffReceiver = new ScreenReceiver();
            screenOnOffReceiver.setScreenCallback(new ScreenReceiver.ScreenCallback() {

                @Override
                public void onScrenOn() {
                    handler.removeCallbacks(runnable);
                    handler.post(runnable);
                }

                @Override
                public void onScrenOff() {
                    handler.removeCallbacks(runnable);
                }
            });
            screenOnOffReceiver.onCreate(this);
        }
    }

    private void initializeAppLockerNotification() {
        ServiceNotificationManager serviceNotificationManager = ServiceNotificationManager.getInstance(this);
        serviceNotificationManager.setClickNotificationListener(this);
        startForeground(ServiceNotificationManager.NOTIFICATION_ID_SERVICE
                , serviceNotificationManager.showNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Config.ActionIntent.ACTION_START_DETECTION_PROXIMITY:
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_PROXIMITY);
                    if (deviceSensorManager != null) {
                        deviceSensorManager.init(Config.DETECTION_PROXIMITY);
                    }
                    break;
                case Config.ActionIntent.ACTION_START_DETECTION_MOTION:
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_MOTION);
                    if (deviceSensorManager != null) {
                        deviceSensorManager.init(Config.DETECTION_MOTION);
                    }
                    break;
                case Config.ActionIntent.ACTION_START_DETECTION_CHARGER:
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE);
                    break;
                case Config.ActionIntent.ACTION_START_DETECTION_HANDSFREE:
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_HANDSFREE);
                    break;
                case Config.ActionIntent.ACTION_START_DETECTION_WIFI:
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_WIFI);
                    break;
                case Config.ActionIntent.ACTION_START_DETECTION_FULL_BATTERY:
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_FULL_BATTERY);
                    break;
                case Config.ActionIntent.ACTION_START_ALARM_CHARGER:
                case Config.ActionIntent.ACTION_START_ALARM_FULL_BATTERY:
                case Config.ActionIntent.ACTION_START_ALARM_HANDSFREE:
                case Config.ActionIntent.ACTION_START_ALARM_WIFI_DISCONNECTED:
                    if (alarmDuringAntiTheftManager != null && !alarmDuringAntiTheftManager.isActiveAlarm()) {
                        onAlarmAntiTheft();
                    }
                    break;
                case Config.ActionIntent.ACTION_START_ALARM_POWER_CONNECTED:
                    ServiceNotificationManager.getInstance(this).showNotificationChargeConnected();
                    break;
                    // For stop detection

                case Config.ActionIntent.ACTION_STOP_DETECTION_CHARGER:
                case Config.ActionIntent.ACTION_STOP_DETECTION_HANDSFREE:
                case Config.ActionIntent.ACTION_STOP_DETECTION_WIFI:
                case Config.ActionIntent.ACTION_STOP_DETECTION_FULL_BATTERY:
                    if (alarmDuringAntiTheftManager != null) {
                        alarmDuringAntiTheftManager.offAlarmAntiTheft();
                    }
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE);
                    checkStopService();
                    break;
                case Config.ActionIntent.ACTION_STOP_ANTI_THEFT:
                case Config.ActionIntent.ACTION_STOP_DETECTION_PROXIMITY:
                case Config.ActionIntent.ACTION_STOP_DETECTION_MOTION:
                    if (deviceSensorManager != null) {
                        deviceSensorManager.unRegisterSensor();
                    }
                    if (alarmDuringAntiTheftManager != null) {
                        alarmDuringAntiTheftManager.offAlarmAntiTheft();
                    }
                    PreferencesHelper.putInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE);
                    checkStopService();
                    break;
                case Config.ActionIntent.ACTION_STOP_SERVICE:
                    checkStopService();
                    break;
            }
        }
        return START_STICKY;
    }

    private void checkStopService() {
        if (PreferencesHelper.getInt(PreferencesHelper.DETECTION_TYPE, Config.DETECTION_NONE) == Config.DETECTION_NONE
                && !PreferencesHelper.getBoolean(PreferencesHelper.ENABLE_KIDZONE, false)) {
            forceStopSerivce();
        }
    }

    private void runLockApp() {
        runnable = () -> {
            lockManager.lockApp();
            handler.postDelayed(runnable, 300);
        };
        handler.post(runnable);
    }

    @Override
    public void onClickNotification(int id) {
        if (alarmDuringAntiTheftManager != null && alarmDuringAntiTheftManager.isActiveAlarm()) {
            gotoCheckPasswordAntiTheft();
        } else {
            Intent intentAct = new Intent(this, MainActivity.class);
            intentAct.setFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (!Toolbox.isAppOnForeground(getApplicationContext())) {
                startActivity(intentAct);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (lockManager != null) {
            lockManager.unregisterInstallUninstallReceiver();
        }
        if (alarmDuringAntiTheftManager != null) {
            alarmDuringAntiTheftManager.offAlarmAntiTheft();
        }
        handler.removeCallbacks(runnable);
        unregisterBoradcastReceiver();
        Intent intent = new Intent(this, WatchmenReceiver.class);
        sendBroadcast(intent);
    }

    private void forceStopSerivce() {
        onDestroy();
        stopSelf();
        stopForeground(true);
    }

    private void unregisterBoradcastReceiver() {
        /** hủy lẳng nghe các sự kiện bảo vệ điện thoại*/
        if (deviceSensorManager != null) {
            deviceSensorManager.unRegisterSensor();
        }
        /** hủy lẳng nghe các sự kiện bảo vệ điện thoại*/
        if (batteryStatusReceiver != null) {
            batteryStatusReceiver.onDestroy(this);
            batteryStatusReceiver = null;
        }

        if (handFreeReceiver != null) {
            handFreeReceiver.onDestroy(this);
            handFreeReceiver = null;
        }

        if (wiFiChangeReceiver != null) {
            wiFiChangeReceiver.onDestroy(this);
            wiFiChangeReceiver = null;
        }
        /** hủy lẳng nghe sự kiện on off màn hình*/
        if (screenOnOffReceiver != null) {
            screenOnOffReceiver.onDestroy(this);
            screenOnOffReceiver = null;
        }
    }

    private void gotoCheckPasswordAntiTheft() {
        Intent intent = new Intent(this, PasswordActivity.class);
        intent.setAction(Config.ActionIntent.ACTION_CHECK_PASSWORD_ANTI_THEFT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    public void onAlarmAntiTheft() {
        if (alarmDuringAntiTheftManager != null && !alarmDuringAntiTheftManager.isActiveAlarm()) {
            alarmDuringAntiTheftManager.alarm();
            gotoCheckPasswordAntiTheft();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDetect() {
        onAlarmAntiTheft();
    }
}
