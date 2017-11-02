package com.canwdev.noise.noise;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.widget.Toast;

import com.canwdev.noise.R;
import com.canwdev.noise.service.BackgroundService;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolUtil;
import com.canwdev.noise.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by CAN on 2017/10/15.
 * Noise类，用于存放声音的各种信息，从/assets读取
 */

public class Noise {
    private static final String TAG = "NOISE##";
    private int imageId;
    private Bitmap imageBmp = null;
    private SoundPoolRandom sounds;
    private String folderName = "";
    private String name = "";
    private boolean loaded = false;
    private boolean loadByFolder = false;

    // 早期构造器
    public Noise(int imageId, String folderName) {
        this.imageId = imageId;
        this.folderName = folderName;
        this.name = folderName;
    }

    // 新构造器，从 assets 自动加载图片与文字（2017/10/24->31）
    public Noise(Context context, String folderName) {
        loadByFolder = true;
        this.folderName = folderName;
        try {
            String[] filenames = context.getResources().getAssets().list(folderName);


            int resultInfo = Arrays.binarySearch(filenames, Conf.F_INFO);
            int resultCover = Arrays.binarySearch(filenames, Conf.F_COVER);

            if (resultInfo > -1) {
                // 读取第一行作为name
                InputStream is_info = context.getResources().getAssets().open(folderName + "/" + Conf.F_INFO);
                InputStreamReader reader = new InputStreamReader(is_info);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer buffer = new StringBuffer("");
                String info_tmp;
                while ((info_tmp = bufferedReader.readLine()) != null) {
                    buffer.append(info_tmp);
                    //buffer.append("\n");
                    break;
                }
                this.name = info_tmp;
                is_info.close();
            } else {
                this.name = folderName;
            }

            if (resultCover > -1) {
                InputStream is_cover = context.getResources().getAssets().open(folderName + "/" + Conf.F_COVER);
                this.imageBmp = BitmapFactory.decodeStream(is_cover);
                is_cover.close();
            } else {
                this.imageId = R.drawable.xxicon;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getImageBmp() {
        return imageBmp;
    }

    public boolean isLoaded() {
        return loaded;
    }

    // 一般是第一次点击条目时，加载SoundPool
    public void loadSoundPool(Context context) {
        if (!loaded) {
            sounds = new SoundPoolRandom(context, folderName, loadByFolder);
            loaded = true;

            // 第一次初始化不发声 ，并发问题
            SoundPoolUtil spu = SoundPoolUtil.getInstance(context);
            spu.play(2);
        } else {

        }
    }

    public int getImageId() {
        return imageId;
    }

    public String getName() {
        return name;
    }

    public SoundPoolRandom getSounds() {
        return sounds;
    }

    // 停止所有播放，
    // 一定要停止BackgroundService服务，在sounds.stop()执行
    public void stopAll() {
        if (loaded) {
            sounds.stop();
            sounds.release();
            sounds.stopEndlessPlay();
            loaded = false;
        }
    }

    /**
     * Created by CAN on 2017/10/14.
     * 本类原来是独立的，后重构成了Noise的子类。
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

            if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnAdvancedInterval, false)) {
                if (endlessPlayTimer == null) {
                    randomIntervalEndlessPlay();
                } else {
                    Toast.makeText(mContext, R.string.toast_adv_cycle_not_allowed, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (endlessPlayTimer == null) {
                    directEndlessPlay();
                } else if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnMultiLoop, true)) {
                    directEndlessPlay();
                } else {
                    Toast.makeText(mContext, R.string.toast_cycle_not_allowed, Toast.LENGTH_SHORT).show();
                }
            }


        }

        // 普通间隔循环播放
        private void directEndlessPlay() {

            Toast.makeText(mContext, Noise.this.getName() + " 循环播放中，共 " + maxSoundCount + " 个文件", Toast.LENGTH_SHORT).show();
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
        private void randomIntervalEndlessPlay() {
            Toast.makeText(mContext, Noise.this.getName() + " 高级间隔播放中，共 " + maxSoundCount + " 个文件", Toast.LENGTH_LONG).show();

            long intervalShort = Integer.parseInt(Util.getDefPref(mContext).getString(Conf.pAdvancedIntervalShort, "2000"));
            long intervalLong = Integer.parseInt(Util.getDefPref(mContext).getString(Conf.pAdvancedIntervalLong, "20000"));

            endlessPlayTimer = new Timer();
            endlessPlayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int times = new Random().nextInt(5);
                    new Thread(() -> {
                        for (int i = 0; i < times; i++) {
                            try {
                                Random random = new Random();
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

        public void stopEndlessPlay() {
            if (endlessPlayTimer != null) {
                endlessPlayTimer.cancel();
                endlessPlayTimer.purge();
                endlessPlayTimer = null;
            }
        }
    }
}
