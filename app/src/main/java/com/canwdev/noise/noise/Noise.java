package com.canwdev.noise.noise;

import android.content.Context;

import com.canwdev.noise.MainActivity;
import com.canwdev.noise.util.SoundPoolUtil;

/**
 * Created by CAN on 2017/10/15.
 */

public class Noise {
    private int imageId;
    private SoundPoolRandom sounds;
    private String folderName = "";
    private boolean loaded = false;

    public boolean isLoaded() {
        return loaded;
    }
// 即将废弃
    /*public Noise(int imageId, SoundPoolRandom sounds) {
        this.imageId = imageId;
        this.sounds = sounds;
        loaded = true;
    }*/

    public Noise(int imageId, String folderName) {
        this.imageId = imageId;
        this.folderName = folderName;
    }

    public void load(Context context) {
        if (!loaded) {
            sounds = new SoundPoolRandom(context, folderName);
            loaded = true;

            // TODO: 2017/10/24 第一次初始化不发声 ，线程问题
            SoundPoolUtil spu = SoundPoolUtil.getInstance(context);
            spu.play(2);
        }
    }

    public int getImageId() {
        return imageId;
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
