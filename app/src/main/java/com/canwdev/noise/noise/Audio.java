package com.canwdev.noise.noise;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by CAN on 2017/11/1.
 */

public class Audio {
    private AssetFileDescriptor afd = null;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public Audio(AssetFileDescriptor afd) {
        this.afd = afd;
        initMusicPlayer();
    }

    public Audio(Context context, String afdPath) {
        try {
            this.afd = context.getAssets().openFd(afdPath);
            initMusicPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMusicPlayer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
        try {
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(e -> {
                mediaPlayer.start();
            });
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
            initMusicPlayer();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
}
