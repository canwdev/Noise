package com.canwdev.noise.noise;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import com.canwdev.noise.util.SoundPoolUtil;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by CAN on 2017/10/15.
 */

public class Noise {
    private int imageId;
    private SoundPoolRandom sounds;
    private String folderName = "";
    private String name = "";
    private boolean loaded = false;

    public Noise(int imageId, String folderName) {
        this.imageId = imageId;
        this.folderName = folderName;
        this.name = folderName;
    }

    // TODO: 2017/10/24 从 assets 加载图片与文字
    public Noise(Context context, String folderName) {
        try {
            String[] filenames = context.getResources().getAssets().list(folderName);

            int resultInfo = Arrays.binarySearch(filenames, "info");
            AssetFileDescriptor infoFile;
            int resultCover = Arrays.binarySearch(filenames, "cover.jpg");
            AssetFileDescriptor coverFile;

            if (resultInfo > 0) {
                infoFile = context.getResources()
                        .getAssets().openFd(folderName + "/" + "info");
                // 删除数组中的"info"
                for (int i = 0; i < filenames.length; i++) {
                    if (filenames[i] == "info") {
                        for (int j = i; j < filenames.length - 1; j++) {
                            filenames[j] = filenames[j + 1];
                        }
                        filenames = Arrays.copyOf(filenames, filenames.length - 1);
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load(Context context) {
        if (!loaded) {
            sounds = new SoundPoolRandom(context, folderName);
            loaded = true;

            // 第一次初始化不发声 ，线程问题
            SoundPoolUtil spu = SoundPoolUtil.getInstance(context);
            spu.play(2);
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

    public void stopAll() {
        if (loaded) {
            sounds.stop();
            sounds.release();
            sounds.stopEndlessPlay();
            loaded = false;
        }
    }
}
