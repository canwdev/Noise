package com.canwdev.noise.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;

import com.canwdev.noise.MainActivity;
import com.canwdev.noise.R;
import com.canwdev.noise.noise.Audio;

import java.text.ParseException;

public class BackgroundService extends Service {
    private Audio bgm;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bgm = new Audio(this, "bgm/mind_beacon_low.mp3");
        bgm.play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bgm.stop();
        bgm.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 这个服务只是为了保证息屏播放的稳定
        createNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    // 显示一条前台通知
    private void createNotification() {
        try {
            startForeground(1, getNotification());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Notification getNotification() throws ParseException {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setContentText("正在播放");
        return builder.build();
    }

}
