package com.canwdev.noise.noise;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Parcel;
import android.util.Log;

import com.canwdev.noise.service.BackgroundService;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by CAN on 2017/10/14.
 * 本类原来是Noise的子类。
 * 主要负责加载SoundPool、播放、无限随机播放、停止等功能
 */
public class SoundPoolRandom implements Serializable {

    private static final String TAG = "SPR##";
    private SoundPool sounds = null;
    private int maxSoundCount = 0;
    private String folderName;
    private Timer endlessPlayTimer = null;
    private Context mContext;
    private Random random = null;
    private int[] randomIdArray;
    private int randomIdArrayIndex;
    private String[] filenames;

    SoundPoolRandom(Context context, String folderName, boolean isLoadByFolder) {
        this.mContext = context;
        this.folderName = folderName;
        try {
            filenames = context.getResources().getAssets().list(folderName);

            // 如果是直接从文件夹加载
            if (isLoadByFolder) {
                // 删除数组中的Conf.F_INFO
                filenames = Util.deleteElementFromArray(filenames, Conf.F_INFO);

                // 删除数组中的Conf.F_COVER
                filenames = Util.deleteElementFromArray(filenames, Conf.F_COVER);
            }
            Log.d(TAG, "SoundPoolRandom: " + Arrays.toString(filenames));
            maxSoundCount = filenames.length;
            randomIdArray = new int[maxSoundCount];
            sounds = new SoundPool(maxSoundCount, AudioManager.STREAM_MUSIC, 0);

            AssetFileDescriptor descriptor;
            for (int i = 0; i < maxSoundCount; i++) {
                descriptor = context.getResources().getAssets().openFd(folderName + "/" + filenames[i]);
                sounds.load(descriptor, 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected SoundPoolRandom(Parcel in) {
        maxSoundCount = in.readInt();
        folderName = in.readString();
        randomIdArray = in.createIntArray();
        randomIdArrayIndex = in.readInt();
        filenames = in.createStringArray();
    }

    public String[] getFilenames() {
        return filenames;
    }

    public boolean isEndlessPlaying() {
        return endlessPlayTimer != null;
    }

    // 尽量不重复的创建随机数组
    private void initRandom() {
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
    private int randomId() {
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
        sounds.play(randomId(), 1, 1, 1, 0, 1f);
    }

    public void playById(int id) {
        sounds.play(id, 1, 1, 1, 0, 1f);
    }

    public void playByIdLoop(int id) {
        sounds.play(id, 1, 1, 1, -1, 1f);
    }

    void release() {
        sounds.release();
    }

    void stop() {
        // 停止BackgroundService服务
        Intent intent = new Intent(mContext, BackgroundService.class);
        mContext.stopService(intent);

        for (int i = 1; i <= maxSoundCount; i++) {
            sounds.stop(i);
        }
    }

    void stop2() {
        for (int i = 1; i <= maxSoundCount; i++) {
            sounds.stop(i);
        }
    }

    int getMaxSoundCount() {
        return maxSoundCount;
    }

    // 普通间隔循环播放
    void directEndlessPlay() {
        startService();
        long interval = Integer.parseInt(Util.getDefPref(mContext).getString(Conf.pAuPlInterval, "1500"));

        endlessPlayTimer = new Timer();
        endlessPlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                play();
            }
        }, 0, interval);
    }

    // 高级间隔循环播放
    void advancedEndlessPlay() {
        startService();

        // 最大重复次数
        int tempCount = Integer.parseInt(Util.getDefPref(mContext).getString(Conf.pAdvancedIntervalTimes, "5")) - 1;
        int maxCount;
        if (tempCount < 2) maxCount = 2;
        else maxCount = tempCount;
        // 秒
        double tempShort = Double.parseDouble(Util.getDefPref(mContext).getString(Conf.pAdvancedIntervalShort, "2"));
        long intervalShort = (long) (tempShort * 1000);
        // 分钟
        double tempLong = Double.parseDouble(Util.getDefPref(mContext).getString(Conf.pAdvancedIntervalLong, "1.25"));
        long intervalLong = (long) (tempLong * 1000 * 60);

        endlessPlayTimer = new Timer();
        endlessPlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                int times = (new Random().nextInt(maxCount)) + 1;

                new Thread(() -> {
                    for (int i = 0; i < times; i++) {
                        try {
                            play();
                            Thread.sleep(intervalShort);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }, 0, intervalLong);
    }

    void stopEndlessPlay() {
        if (endlessPlayTimer != null) {
            endlessPlayTimer.cancel();
            endlessPlayTimer.purge();
            endlessPlayTimer = null;
        }
    }

    private void startService() {
        // 启动BackgroundService服务
        // 注意：这在targetApi 26+ 不可直接使用
        Intent intent = new Intent(mContext, BackgroundService.class);
        mContext.startService(intent);
    }
}
