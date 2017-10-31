package com.canwdev.noise.noise;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.widget.Toast;

import com.canwdev.noise.service.BackgroundService;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by CAN on 2017/10/14.
 * 本来可以是Noise的子类，但独立出来了
 * 主要负责加载SoundPool、播放、无限随机播放、停止等功能
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
    private String[] filenames;

    public SoundPoolRandom(Context context, String folderName, boolean loadByFolder) {
        mContext = context;
        this.folderName = folderName;
        try {
            filenames = context.getResources().getAssets().list(folderName);

            // 如果是直接从文件夹加载
            if (loadByFolder) {
                // 删除数组中的Conf.F_INFO
                for (int i = 0; i < filenames.length; i++) {
                    if (filenames[i].equals(Conf.F_INFO)) {
                        for (int j = i; j < filenames.length - 1; j++) {
                            filenames[j] = filenames[j + 1];
                        }
                        filenames = Arrays.copyOf(filenames, filenames.length - 1);
                        break;
                    }
                }
                // 删除数组中的Conf.F_COVER
                for (int i = 0; i < filenames.length; i++) {
                    if (filenames[i].equals(Conf.F_COVER)) {
                        for (int j = i; j < filenames.length - 1; j++) {
                            filenames[j] = filenames[j + 1];
                        }
                        filenames = Arrays.copyOf(filenames, filenames.length - 1);
                        break;
                    }
                }
            }
            Log.d(TAG, "SoundPoolRandom: " + Arrays.toString(filenames));
            maxSoundCount = filenames.length;
            randomIdArray = new int[maxSoundCount];
            sound = new SoundPool(maxSoundCount, AudioManager.STREAM_MUSIC, 0);

            AssetFileDescriptor descriptor = null;
            for (int i = 0; i < maxSoundCount; i++) {
                descriptor = context.getResources().getAssets().openFd(folderName + "/" + filenames[i]);
                sound.load(descriptor, 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 尽量不重复的创建随机数组
    public void initRandom() {
        random = new Random();
        for (int i = 0; i < randomIdArray.length; i++) {
            int number = random.nextInt(maxSoundCount) + 1;

            for (int j = 0; j <= i; j++) {
                if (number != randomIdArray[j]) {
                    randomIdArray[i] = number;
                }
            }
        }
        Log.d(TAG, "randomId: " + Arrays.toString(randomIdArray));
        randomIdArrayIndex = 0;
    }

    // 从随机数组（randomIdArray）中顺序读取，读完重新生成随机数组，以避免重复
    public int randomId() {
        if (random == null) {
            initRandom();
            return randomIdArray[randomIdArrayIndex];
        } else {
            if (randomIdArrayIndex < randomIdArray.length - 1) {
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
        // 停止BackgroundService服务
        Intent intent = new Intent(mContext, BackgroundService.class);
        mContext.stopService(intent);

        for (int i = 1; i <= maxSoundCount; i++) {
            sound.stop(i);
        }
    }

    // [主要]无尽随机播放，若需停止，使用 noise.stopAll();
    public void endlessPlay() {
        // 启动BackgroundService服务
        // 注意：这在targetApi 26+ 不可直接使用
        Intent intent = new Intent(mContext, BackgroundService.class);
        mContext.startService(intent);

        if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnRandomInterval, false)) {
            if (endlessPlayTimer == null) {
                randomIntervalDndlessPlay();
            } else {
                Toast.makeText(mContext, "随机间隔循环不允许重复循环播放", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (endlessPlayTimer == null) {
                directEndlessPlay();
            } else if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnMultiLoop, true)) {
                directEndlessPlay();
            } else {
                Toast.makeText(mContext, "不允许重复循环播放，请到设置修改", Toast.LENGTH_SHORT).show();
            }
        }


    }

    // 普通间隔循环播放
    private void directEndlessPlay() {

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

    // 随机间隔循环播放
    private void randomIntervalDndlessPlay() {
        // TODO: 2017/10/24 随机间隔循环播放
        Toast.makeText(mContext, "未实现：随机间隔循环播放功能开发中...", Toast.LENGTH_SHORT).show();
    }

    public void stopEndlessPlay() {
        if (endlessPlayTimer != null) {
            endlessPlayTimer.cancel();
            endlessPlayTimer.purge();
            endlessPlayTimer = null;
        }
    }
}
