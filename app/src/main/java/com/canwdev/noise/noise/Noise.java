package com.canwdev.noise.noise;

import android.content.Context;

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
        // TODO: 2017/10/24 第一次初始化不发声 ，线程问题
        if (!loaded) {
            sounds = new SoundPoolRandom(context, folderName);
            loaded = true;
        }
    }

    public void unload() {
        sounds.release();
        loaded = false;
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
