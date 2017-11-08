package com.canwdev.noise;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.canwdev.noise.noise.Noise;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TroubleMakerActivity extends AppCompatActivity {

    static final private double EMA_FILTER = 0.6;
    private static double mEMA = 0.0;
    private final Handler mHandler = new Handler();
    private String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    // 控件
    private TextView textView_status_dB;
    private TextView textView_sensitivity;
    private SeekBar seekBar_sensitivity;
    private Button button_chooseNoiseSet;
    private TextView textView_status_playing;
    // dB
    private MediaRecorder mRecorder;
    private Thread runner;
    private long[] sensitivity = {50, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 9999999};
    private int sensitivityLevel = 1;
    private List<Noise> noiseList = new ArrayList<>();
    private final Runnable updater = () -> updateTv();
    private int noiseListId = 0;

    private void initNoises() {
        noiseList.add(new Noise(R.drawable.ra_box, "ra2/audio_box"));
        noiseList.add(new Noise(R.drawable.ra_boom, "ra2/audio_boom"));
        noiseList.add(new Noise(R.drawable.ra_gun, "ra2/audio_gun"));
        noiseList.add(new Noise(R.drawable.ra_allied_base, "ra2/audio_base"));

        // 从指定文件夹自动查询/assets/guichu子文件夹名称，并通过重载的构造器自动加载内容
        try {
            String[] folders = getResources().getAssets().list("guichu");
            for (String folderName : folders) {
                noiseList.add(new Noise(this, "guichu/" + folderName));
            }

            // 测试用素材
            folders = getResources().getAssets().list("testres");
            for (String folderName : folders) {
                noiseList.add(new Noise(this, "testres/" + folderName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        noiseList.get(noiseListId).loadSoundPool(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouble_maker);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        textView_status_dB = (TextView) findViewById(R.id.status_dB);
        textView_sensitivity = (TextView) findViewById(R.id.textView_sensitivity);
        seekBar_sensitivity = (SeekBar) findViewById(R.id.seekBar_sensitivity);
        button_chooseNoiseSet = (Button) findViewById(R.id.button_chooseNoiseSet);
        textView_status_playing = (TextView) findViewById(R.id.status_playing);

        seekBar_sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensitivityLevel = progress;
                textView_sensitivity.setText(sensitivity[sensitivityLevel] + " dB");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        initNoises();
    }

    public void updateTv() {
        double dB = getAmplitudeEMA();

        String show = String.format("%.0f dB", dB);
        textView_status_dB.setText(show);
        // TODO: 2017/11/8 写注释,加功能
        if (dB > sensitivity[sensitivityLevel]) {
            textView_status_playing.setText("WARNING");
            textView_status_playing.setTextColor(Color.RED);

            noiseList.get(noiseListId).getSounds().play();

            int temp = sensitivityLevel;
            setSensitivity(11);
            seekBar_sensitivity.setEnabled(false);

            Timer stopTimer = new Timer();
            stopTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        setSensitivity(temp);
                        seekBar_sensitivity.setEnabled(true);
                    });
                }
            }, 7000); //delay 后启动一次
        } else {
            textView_status_playing.setText("OK");
            textView_status_playing.setTextColor(Color.GREEN);
        }
    }

    private void setSensitivity(int i) {
        sensitivityLevel = i;
        textView_sensitivity.setText(sensitivity[sensitivityLevel] + " dB");
        seekBar_sensitivity.setProgress(i);
    }


    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(
                this, PERMISSION_RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this
                    , new String[]{PERMISSION_RECORD_AUDIO}, 1);
        } else {
            startRecorder();
            getNoiseLevel();
        }
    }

    public void onPause() {
        super.onPause();
        stopRecorder();
    }

    public void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " +
                        android.util.Log.getStackTraceString(ioe));

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " +
                        android.util.Log.getStackTraceString(e));
            }
            try {
                mRecorder.start();
            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " +
                        android.util.Log.getStackTraceString(e));
            }

            //mEMA = 0.0;
        }

    }

    private void getNoiseLevel() {
        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(100);
                            //Log.i("Noise", "Tock");
                        } catch (InterruptedException e) {
                        }

                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            //Log.d("Noise", "start runner()");
        }
    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double soundDb(double ampl) {
        return 20 * Math.log10(getAmplitudeEMA() / ampl);
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    // requestPermissions() 回调，判断是否授予了权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecorder();
                    getNoiseLevel();
                } else {
                    Toast.makeText(this, "Permission denied: \n" + PERMISSION_RECORD_AUDIO
                            , Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
