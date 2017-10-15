package com.canwdev.noise.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;

import java.io.IOException;
import java.util.Random;

/**
 * Created by CAN on 2017/10/14.
 */

public class SoundPoolRandom {
    private SoundPool sound = null;
    private int maxSoundCount = 0;
    private String folderName;
    public SoundPoolRandom(Context context, String folderName, int Max) {
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


}
