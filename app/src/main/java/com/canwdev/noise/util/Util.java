package com.canwdev.noise.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by CAN on 2017/10/14.
 */

public class Util {
    private static final String TAG = "##TAG";

    public static AssetFileDescriptor loadAssetRandomly(Context context, String folder) {
        String randAudio = "";
        AssetFileDescriptor descriptor = null;
        try {
            String[] audios = context.getResources().getAssets().list(folder);
            Random r = new Random();
            randAudio = audios[r.nextInt(audios.length)];
            descriptor = context.getResources().getAssets().openFd(folder + "/" + randAudio);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "loadAssetRandomly: " + randAudio);

        return descriptor;
    }

    //将毫秒转化为 时：分：秒 格式 ，例如  00:05:23
    public static String calculatTime(int milliSecondTime) {

        int hour = milliSecondTime / (60 * 60 * 1000);
        int minute = (milliSecondTime - hour * 60 * 60 * 1000) / (60 * 1000);
        int seconds = (milliSecondTime - hour * 60 * 60 * 1000 - minute * 60 * 1000) / 1000;

        if (seconds >= 60) {
            seconds = seconds % 60;
            minute += seconds / 60;
        }
        if (minute >= 60) {
            minute = minute % 60;
            hour += minute / 60;
        }

        String sh = "";
        String sm = "";
        String ss = "";
        if (hour < 10) {
            sh = "0" + String.valueOf(hour);
        } else {
            sh = String.valueOf(hour);
        }
        if (minute < 10) {
            sm = "0" + String.valueOf(minute);
        } else {
            sm = String.valueOf(minute);
        }
        if (seconds < 10) {
            ss = "0" + String.valueOf(seconds);
        } else {
            ss = String.valueOf(seconds);
        }

        return sh + ":" + sm + ":" + ss;
    }

    // 获取默认的 SharedPreferences
    public static SharedPreferences getDefPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    // 获取自定义 SharedPreferences
    public static SharedPreferences getPref(Context context, String PREF_FILE_NAME) {
        return context.getSharedPreferences(PREF_FILE_NAME, context.MODE_PRIVATE);
    }

    private void stopTime() {
        /*final Calendar calendar = Calendar.getInstance();
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {

                        calendar.set(Calendar.HOUR_OF_DAY, i);
                        calendar.set(Calendar.MINUTE, i1);
                        long timer = calendar.getTime().getTime();
                        Log.d(TAG, "timer: " + timer);
                        long now = new Date().getTime();
                        Log.d(TAG, "now--: " + now);
                        if (now < timer) {
                            long diff = timer - now;
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                            String hms = sdf.format(diff);

                            Toast.makeText(MainActivity.this, "将在以下时间后停止: " + hms, Toast.LENGTH_SHORT).show();
                            stopTimer = new Timer();
                            stopTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    for (Noise n : noiseList) {
                                        n.stopAll();
                                    }
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.this, "循环播放停止", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                    *//**//*finish();
                                    System.exit(0);*//**//*
                                }
                            }, diff); //diff秒后启动一次

                        } else {
                            Toast.makeText(MainActivity.this, "选择的时间无效", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();*/
    }
}
