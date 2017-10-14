package com.canwdev.noise.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.canwdev.noise.R;

import static com.canwdev.noise.util.Util.loadAssetRandomly;

/**
 * Created by CAN on 2017/10/14.
 */

public class SoundPoolUtil {
    private static SoundPoolUtil soundPoolUtil;
    private SoundPool soundPool;

    private SoundPoolUtil(Context context) {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 5);
        //加载音频文件
        soundPool.load(context, R.raw.slide1, 1);
        soundPool.load(context, R.raw.slide2, 1);
    }

    //单例模式
    public static SoundPoolUtil getInstance(Context context) {
        if (soundPoolUtil == null)
            soundPoolUtil = new SoundPoolUtil(context);
        return soundPoolUtil;
    }

    /**
     *
     * @param context
     * @deprecated
     */
    public static void playAssetsRandomBad(Context context) {
        final SoundPool sound = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        sound.load(loadAssetRandomly(context, "audio_box"), 1);
        sound.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                sound.play(1, 1, 1, 0, 0, 1);
            }
        });
    }



    public void play(int number) {
        //播放音频
        soundPool.play(number, 1, 1, 0, 0, 1);
    }


}
