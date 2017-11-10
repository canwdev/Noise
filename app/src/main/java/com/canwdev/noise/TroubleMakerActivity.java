package com.canwdev.noise;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.canwdev.noise.noise.Noise;
import com.canwdev.noise.util.ActivityCollector;
import com.canwdev.noise.util.BaseActivity;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class TroubleMakerActivity extends BaseActivity {

    static final private double EMA_FILTER = 0.6;
    static final private long INTERVAL_RECORD = 200;
    static final private long INTERVAL_DELAY = 5000;
    private static final int INTENT_STOP_TIME = 1;
    private static double mEMA = 0.0;
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

        noiseList.get(noiseListId).loadSoundPoolSilent(this);
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
                if (!(sensitivityLevel == sensitivity.length - 1)) {
                    textView_sensitivity.setText(sensitivity[sensitivityLevel] + "");
                    Util.getDefPref(TroubleMakerActivity.this).edit()
                            .putInt(Conf.sensitivityLevel, progress).apply();
                } else {
                    textView_sensitivity.setText("MAX");

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        noiseListId = Util.getDefPref(this).getInt(Conf.noiseListId, 0);
        setSensitivity(Util.getDefPref(this).getInt(Conf.sensitivityLevel, 1), true);
        initNoises();

        button_chooseNoiseSet.setOnClickListener(v -> {
            String[] noiseNames = new String[noiseList.size()];

            for (int i = 0; i < noiseList.size(); i++) {
                noiseNames[i] = noiseList.get(i).getName();
            }

            int itemSelected = noiseListId;
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.choose_noise_set))
                    .setSingleChoiceItems(noiseNames, itemSelected, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            noiseList.get(i).loadSoundPoolSilent(TroubleMakerActivity.this);
                            noiseListId = i;
                            noiseList.get(i).getSounds().play();
                            Util.getDefPref(TroubleMakerActivity.this).edit()
                                    .putInt(Conf.noiseListId, noiseListId).apply();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    public void updateTv() {
        double dB = getAmplitudeEMA();

        String show = String.format("%.0f", dB);
        textView_status_dB.setText(show);
        // TODO: 2017/11/8 写注释

        long dBLong = (long) dB;
        if (dBLong > sensitivity[sensitivityLevel]) {
            setSensitivity(sensitivity.length - 1, false);

            if (!seekBar_sensitivity.isEnabled())
                noiseList.get(noiseListId).getSounds().play();


            Timer stopTimer = new Timer();
            stopTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        setSensitivity(Util.getDefPref(TroubleMakerActivity.this).getInt(Conf.sensitivityLevel, 1), true);
                    });
                }
            }, INTERVAL_DELAY); //delay 后启动一次
        } else {
            if (seekBar_sensitivity.isEnabled()) {
                textView_status_playing.setText("OK");
                textView_status_playing.setTextColor(Color.GREEN);
            } else {
                textView_status_playing.setText("WARNING");
                textView_status_playing.setTextColor(Color.RED);
            }
        }
    }

    private void setSensitivity(int i, boolean enableSeekBar) {
        sensitivityLevel = i;
        textView_sensitivity.setText(sensitivity[sensitivityLevel] + "");
        seekBar_sensitivity.setProgress(i);
        seekBar_sensitivity.setEnabled(enableSeekBar);
        if (enableSeekBar) {
            Util.getDefPref(TroubleMakerActivity.this).edit()
                    .putInt(Conf.sensitivityLevel, sensitivityLevel).apply();
        }
    }

    @Override
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecorder();
        for (Noise n : noiseList) {
            n.stopAll();
        }
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
                            Thread.sleep(INTERVAL_RECORD);
                            //Log.i("Noise", "Tock");
                        } catch (InterruptedException e) {
                        }
                        runOnUiThread(() -> {
                            updateTv();
                        });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_stop:
                setSensitivity(sensitivity.length - 1, true);
                break;
            case R.id.menu_stop_timer:
                // 定时停止播放
                Intent intent = new Intent(TroubleMakerActivity.this, SetTimeActivity.class);
                startActivityForResult(intent, INTENT_STOP_TIME);
                break;
            default:
                break;

        }
        return true;
    }

    // 获取时间选择Activity的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_STOP_TIME:
                if (resultCode == RESULT_OK) {
                    long millisecond = data.getLongExtra("millisecond", -1);
                    boolean autoExit = data.getBooleanExtra("autoExit", true);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    String hms = sdf.format(millisecond);

                    if (millisecond > 0) {
                        Toast.makeText(TroubleMakerActivity.this, "将在 " + hms + " 后停止", Toast.LENGTH_LONG).show();
                        Timer stopTimer = new Timer();
                        stopTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finish();
                                if (autoExit) {
                                    ActivityCollector.finishAll(TroubleMakerActivity.this);
                                }
                            }
                        }, millisecond); //millisecond 后启动一次
                    } else {
                        Toast.makeText(TroubleMakerActivity.this, R.string.toast_invalid_time, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
        }
    }
}
