package com.canwdev.noise.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by CAN on 2017/10/14.
 */

public class SoundPoolRandom {
    private SoundPool sound = null;
    private int maxSoundCount = 0;
    private String folderName;
    private Timer endlessPlayTimer;
    private Context mContext;

    public SoundPoolRandom(Context context, String folderName, int Max) {
        mContext = context;
        this.folderName = folderName;
        sound = new SoundPool(Max, AudioManager.STREAM_MUSIC, 5);
        try {
            String randAudio = "";
            AssetFileDescriptor descriptor = null;
            String[] audios = context.getResources().getAssets().list(folderName);
            maxSoundCount = audios.length;
            for (int i = 0; i < maxSoundCount; i++) {
                descriptor = context.getResources().getAssets().openFd(folderName + "/" + audios[i]);
                sound.load(descriptor, 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        Random r = new Random();
        int id = r.nextInt(maxSoundCount);
        if (id == 0) id = 1;
        sound.play(id, 1, 1, 0, 0, 1);
    }

    public void release(){
        sound.release();
    }

    public String getFolderInfo() {
        return folderName+"("+maxSoundCount+")";
    }

    public void endlessPlay() {

        Toast.makeText(mContext, getFolderInfo() + " 循环播放中", Toast.LENGTH_SHORT).show();
        long interval = Integer.parseInt(Util.getDefPref(mContext).getString(Conf.pAuPlInterval, "500"));
        endlessPlayTimer = new Timer();
        endlessPlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                play();
            }
        }, 0, interval);
    }

    public void stopEndlessPlay() {
        if (endlessPlayTimer != null) {
            Toast.makeText(mContext, "循环播放停止", Toast.LENGTH_SHORT).show();
            endlessPlayTimer.cancel();
            endlessPlayTimer.purge();
            endlessPlayTimer = null;
        }
    }
}
