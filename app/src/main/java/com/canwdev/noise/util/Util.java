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
}
