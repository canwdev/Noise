package com.canwdev.noise.noise;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

/**
 * Created by CAN on 2017/11/1.
 */

public class Audio implements Parcelable {
    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };
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

    protected Audio(Parcel in) {
        afd = in.readParcelable(AssetFileDescriptor.class.getClassLoader());
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

    public void playLoop() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(e -> mediaPlayer.start());
        }
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void reset() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
    }

    public void stopAndReset() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(afd, flags);
    }
}
