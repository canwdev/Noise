package com.canwdev.noise.noise;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.canwdev.noise.R;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by CAN on 2017/10/15.
 * Noise类，用于存放声音的各种信息，从/assets读取
 */

public class Noise implements Parcelable{

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

    void setLoaded(boolean loaded) {
        this.loaded = loaded;
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
                StringBuilder buffer = new StringBuilder("");
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

    protected Noise(Parcel in) {
        imageId = in.readInt();
        imageBmp = in.readParcelable(Bitmap.class.getClassLoader());
        folderName = in.readString();
        name = in.readString();
        loaded = in.readByte() != 0;
        loadByFolder = in.readByte() != 0;
    }

    public static final Creator<Noise> CREATOR = new Creator<Noise>() {
        @Override
        public Noise createFromParcel(Parcel in) {
            return new Noise(in);
        }

        @Override
        public Noise[] newArray(int size) {
            return new Noise[size];
        }
    };

    Bitmap getImageBmp() {
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
        }
    }

    int getImageId() {
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

    public void stopAll2() {
        if (loaded) {
            sounds.stop2();
            sounds.release();
            sounds.stopEndlessPlay();
            loaded = false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageId);
        dest.writeParcelable(imageBmp, flags);
        dest.writeString(folderName);
        dest.writeString(name);
        dest.writeByte((byte) (loaded ? 1 : 0));
        dest.writeByte((byte) (loadByFolder ? 1 : 0));
    }
}
