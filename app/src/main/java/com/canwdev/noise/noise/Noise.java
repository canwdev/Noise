package com.canwdev.noise.noise;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

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

                if (resultCover > -1) {
                    InputStream is_cover = context.getResources().getAssets().open(folderName + "/" + Conf.F_COVER);
                    this.imageBmp = BitmapFactory.decodeStream(is_cover);
                    is_cover.close();
                }
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
}
