package com.canwdev.noise.noise;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.canwdev.noise.R;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolUtil;
import com.canwdev.noise.util.Util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by CAN on 2017/10/15.
 * Noise类，用于存放声音的各种信息，从/assets读取
 */

public class Noise implements Parcelable {

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
    private static final String TAG = "NOISE##";
    private int imageId;
    private Bitmap imageBmp = null;
    private SoundPoolRandom sounds;
    // This is audio, audio与SoundPool不可同时使用，使用audio时，SoundPool会被忽略
    private Audio[] audios = null;
    private String[] audioFileNames = null;
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

    // 新构造器
    public Noise(Context context, String folderName) {
        loadByFolder = true;
        this.folderName = folderName;
        getInfoFromAssets(context, folderName);
    }

    // Audio 构造器，使用后必须再用 initAudio()
    public Noise(Context context, String folderName, boolean isAudio) throws IOException {
        String[] fAudios = context.getResources().getAssets().list(folderName);

        fAudios = Util.deleteElementFromArray(fAudios, Conf.F_INFO);
        fAudios = Util.deleteElementFromArray(fAudios, Conf.F_COVER);

        audioFileNames = new String[fAudios.length];
        for (int i = 0; i < fAudios.length; i++) {
            audioFileNames[i] = folderName + "/" + fAudios[i];
        }

        getInfoFromAssets(context, folderName);
    }

    protected Noise(Parcel in) {
        imageId = in.readInt();
        // 大图片闪退，已使用专用方法传递
        // imageBmp = in.readParcelable(Bitmap.class.getClassLoader());
        audios = in.createTypedArray(Audio.CREATOR);
        audioFileNames = in.createStringArray();
        folderName = in.readString();
        name = in.readString();
        loaded = in.readByte() != 0;
        loadByFolder = in.readByte() != 0;
    }

    // 从 assets 自动加载图片与文字（2017/10/24->31）
    private void getInfoFromAssets(Context context, String folderName) {
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
                Bitmap temp = BitmapFactory.decodeStream(is_cover);

                // 加载时先压缩一遍
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                temp.compress(Bitmap.CompressFormat.JPEG, 90, out);
                ByteArrayInputStream isBm = new ByteArrayInputStream(out.toByteArray());

                this.imageBmp = BitmapFactory.decodeStream(isBm);

                is_cover.close();
            } else {
                this.imageId = R.drawable.xxicon;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 初始化audios
    public void initAudio(Context context) {
        Audio[] audios = new Audio[audioFileNames.length];
        for (int i = 0; i < audioFileNames.length; i++) {
            audios[i] = new Audio(context, audioFileNames[i]);
        }
        this.audios = audios;
    }

    public boolean isAudio() {
        return audioFileNames != null;
    }

    Bitmap getImageBmp() {
        return imageBmp;
    }

    public boolean isLoaded() {
        return loaded;
    }

    void setLoaded(boolean loaded) {
        this.loaded = loaded;
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

    public Audio[] getAudios() {
        return audios;
    }

    public Audio getAudio(int i) {
        return audios[i];
    }

    public String[] getAudioFileNames() {
        return audioFileNames;
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
        if (isAudio()) {
            for (Audio a : audios) {
                a.stop();
                a.reset();
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(imageId);
        // dest.writeParcelable(imageBmp, flags);
        dest.writeTypedArray(audios, flags);
        dest.writeStringArray(audioFileNames);
        dest.writeString(folderName);
        dest.writeString(name);
        dest.writeByte((byte) (loaded ? 1 : 0));
        dest.writeByte((byte) (loadByFolder ? 1 : 0));
    }
}
