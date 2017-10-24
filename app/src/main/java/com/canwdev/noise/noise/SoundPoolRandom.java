package com.canwdev.noise.noise;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.widget.Toast;

import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

import java.io.IOException;
import java.util.Arrays;
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
    private Random random = null;
    private int[] randomIdArray;
    private int randomIdArrayIndex;

    public SoundPoolRandom(Context context, String folderName) {
        mContext = context;
        this.folderName = folderName;
        try {
            String[] audios = context.getResources().getAssets().list(folderName);
            maxSoundCount = audios.length;
            randomIdArray = new int[maxSoundCount];
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

    // 尽量不重复（玄学随机??）
    public void initRandom(){
        random = new Random();
        for (int i = 0; i < randomIdArray.length; i++) {
            int number = random.nextInt(maxSoundCount) + 1;

            for (int j = 0; j <= i; j++) {
                if (number != randomIdArray[j]) {
                    randomIdArray[i] = number;
                }
            }
        }
        Log.d(TAG, "randomId: "+ Arrays.toString(randomIdArray));
        randomIdArrayIndex = 0;
    }


    public int randomId() {
        if (random == null) {
            initRandom();
            return randomIdArray[randomIdArrayIndex];
        } else {
            if (randomIdArrayIndex < randomIdArray.length-1) {
                ++randomIdArrayIndex;
                return randomIdArray[randomIdArrayIndex];
            } else {
                random = null;
                initRandom();
                return randomIdArray[randomIdArrayIndex];

            }
        }
    }

    public void play() {
        sound.play(randomId(), 1, 1, 1, 0, 1f);
    }

    public void release() {
        sound.release();
    }

    public void stop() {
        for (int i = 1; i <= maxSoundCount; i++) {
            sound.stop(i);
        }
    }

    public void endlessPlay() {

        if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnRandomInterval, false)) {
            if (endlessPlayTimer == null) {
                randomIntervalDndlessPlay();
            } else {
                Toast.makeText(mContext, "玄学循环不允许重复循环播放", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (endlessPlayTimer == null) {
                directDndlessPlay();
            } else if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnMultiLoop, true)) {
                directDndlessPlay();
            } else {
                Toast.makeText(mContext, "不允许重复循环播放，请到设置修改", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void directDndlessPlay() {
        Toast.makeText(mContext, folderName + " 循环播放中，共 " + maxSoundCount + " 个文件", Toast.LENGTH_SHORT).show();
        long interval = Integer.parseInt(Util.getDefPref(mContext).getString(Conf.pAuPlInterval, "1500"));

        endlessPlayTimer = new Timer();
        endlessPlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                play();
            }
        }, 0, interval);
    }

    private void randomIntervalDndlessPlay() {
        // TODO: 2017/10/24 玄学循环播放
        Toast.makeText(mContext, "未实现：玄学循环播放功能开发中...", Toast.LENGTH_SHORT).show();
    }

    public void stopEndlessPlay() {
        if (endlessPlayTimer != null) {
            endlessPlayTimer.cancel();
            endlessPlayTimer.purge();
            endlessPlayTimer = null;
        }
    }
}
