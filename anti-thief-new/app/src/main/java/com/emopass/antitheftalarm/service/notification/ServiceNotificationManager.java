package com.emopass.antitheftalarm.service.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.emopass.antitheftalarm.R;
import com.emopass.antitheftalarm.ui.main.MainActivity;

public class ServiceNotificationManager {

    public static final int NOTIFICATION_ID_SERVICE = 157;
    public static final int NOTIFICATION_ID_BATTERY_CONNECTED = 158;
    public static final String ACTION_NOTIFICATION_BUTTON_CLICK = "acction click notification";
    public static final String EXTRA_BUTTON_CLICKED = "extra click button";

    private static ServiceNotificationManager instance;

    public static ServiceNotificationManager getInstance(Context context) {
        if (instance == null)
            instance = new ServiceNotificationManager(context);
        return instance;
    }

    private final Context context;
    private ClickNotificationListener clickNotificationListener;
    private NotificationManager notificationManager;

    private ServiceNotificationManager(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public interface ClickNotificationListener {
        void onClickNotification(int id);
    }

    public void setClickNotificationListener(ClickNotificationListener clickNotificationListener) {
        this.clickNotificationListener = clickNotificationListener;
    }

    private void createAppLockerServiceChannel(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(String.valueOf(id), name, importance);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification showNotification() {
        RemoteViews notificationLayout =
                new RemoteViews(context.getPackageName(), R.layout.layout_notification_manager);
        setupViewNotification(notificationLayout);
        IntentFilter intentFilterControl = new IntentFilter();
        intentFilterControl.addAction(ACTION_NOTIFICATION_BUTTON_CLICK);
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {

        }
        context.registerReceiver(receiver, intentFilterControl);
        createAppLockerServiceChannel(NOTIFICATION_ID_SERVICE);
        Notification
                notification = new NotificationCompat.Builder(context, String.valueOf(NOTIFICATION_ID_SERVICE))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        return notification;
    }

    public void showNotificationChargeConnected() {
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID_BATTERY_CONNECTED, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        createAppLockerServiceChannel(NOTIFICATION_ID_BATTERY_CONNECTED);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification_manager);
        Notification
                notification = new NotificationCompat.Builder(context, String.valueOf(NOTIFICATION_ID_BATTERY_CONNECTED))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCustomContentView(remoteViews)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        notificationManager.notify(NOTIFICATION_ID_BATTERY_CONNECTED, notification);
    }

    public void updateNotification() {
        notificationManager.notify(NOTIFICATION_ID_SERVICE, showNotification());
    }

    private void setupViewNotification(RemoteViews notificationLayout) {
        notificationLayout.setTextViewText(R.id.tv_title_notification
                , context.getString(R.mipmap.ic_launcher, context.getString(R.string.app_name)));
        notificationLayout.setOnClickPendingIntent(R.id.container, onButtonNotificationClick(context, R.id.container));
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra(EXTRA_BUTTON_CLICKED, -1);
            if (clickNotificationListener != null)
                clickNotificationListener.onClickNotification(id);
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }
    };

    public PendingIntent onButtonNotificationClick(Context context, @IdRes int id) {
        Intent intent = new Intent(ACTION_NOTIFICATION_BUTTON_CLICK);
        intent.putExtra(EXTRA_BUTTON_CLICKED, id);
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE | 0);
    }

    public void hideNotification(int id) {
        NotificationManagerCompat.from(context).cancel(id);
    }

}
