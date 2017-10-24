package com.canwdev.noise.noise;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;

import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by CAN on 2017/10/14.
 */

public class SoundPoolRandom {
    private static final String TAG = "SPR##";
    private SoundPool sound = null;
    private int maxSoundCount = 0;
    private String folderName;
    private Timer endlessPlayTimer = null;
    private Context mContext;

    public SoundPoolRandom(Context context, String folderName) {
        mContext = context;
        this.folderName = folderName;
        try {
            String[] audios = context.getResources().getAssets().list(folderName);
            maxSoundCount = audios.length;
            sound = new SoundPool(maxSoundCount, AudioManager.STREAM_MUSIC, 0);

            String randAudio = "";
            AssetFileDescriptor descriptor = null;
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
        int id = r.nextInt(maxSoundCount + 1);
        if (id == 0) id = 1;
        sound.play(id, 1, 1, 1, 0, 1f);
    }

    public void release() {
        sound.release();
    }

    public void stop() {
        for (int i = 1; i <= maxSoundCount; i++) {
            sound.stop(i);
        }
    }

    public String getFolderInfo() {
        return folderName + "(" + maxSoundCount + ")";
    }

    public void endlessPlay() {

        if (endlessPlayTimer == null) {
            directDndlessPlay();
        } else if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnLoop, false)) {
            directDndlessPlay();
        } else {
            Toast.makeText(mContext, getFolderInfo() + " 不允许重复循环播放，请到设置修改", Toast.LENGTH_SHORT).show();
        }

    }

    private void directDndlessPlay() {
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
            endlessPlayTimer.cancel();
            endlessPlayTimer.purge();
            endlessPlayTimer = null;
        }
    }
}
