package com.canwdev.noise.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.util.Random;

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
            descriptor = context.getResources().getAssets().openFd(folder+"/"+randAudio);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "loadAssetRandomly: "+randAudio);

        return descriptor;
    }
}
