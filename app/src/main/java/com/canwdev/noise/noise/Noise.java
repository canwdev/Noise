package com.canwdev.noise.noise;

/**
 * Created by CAN on 2017/10/15.
 */

public class Noise {
    private int imageId;
    private SoundPoolRandom sounds;

    public Noise(int imageId, SoundPoolRandom sounds) {
        this.imageId = imageId;
        this.sounds = sounds;
    }

    public int getImageId() {
        return imageId;
    }

    public SoundPoolRandom getSounds() {
        return sounds;
    }


}
