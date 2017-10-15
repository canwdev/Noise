package com.canwdev.noise.noise;

import android.content.Context;
import android.widget.Toast;

import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolRandom;
import com.canwdev.noise.util.Util;

import java.util.Timer;
import java.util.TimerTask;

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
