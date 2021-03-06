package com.canwdev.noise.TroubleMaker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.canwdev.noise.R;
import com.canwdev.noise.SetTimeActivity;
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
    private static final int INTENT_STOP_TIME = 1;
    static final private long INTERVAL_RECORD = 200;
    static private long INTERVAL_DELAY = 6500;
    private final int seekBarMax = 21 + 1;
    private String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    private TextView textView_sensitivity;
    private SeekBar seekBar_sensitivity;
    private TextView textView_status_playing;
    private Recorder mRecorder;
    private long[] sensitivity = new long[seekBarMax];
    private int sensitivityLevel = 10;
    private List<Noise> noiseList = new ArrayList<>();
    private int noiseListId = 0;
    private Timer mDelayTimer;
    private boolean PREFERENCE_ENABLE_DB_MODE;
    private double PREFERENCE_REFERENCE_AMP;
    private Thread runner;

    private void initNoises() {
        noiseList.add(new Noise(R.drawable.ra_box, "Box","ra2/audio_box"));
        noiseList.add(new Noise(R.drawable.ra_boom, "Boom","ra2/audio_boom"));
        noiseList.add(new Noise(R.drawable.ra_gun, "Guns","ra2/audio_gun"));
        noiseList.add(new Noise(R.drawable.ra_allied_base, "Base","ra2/audio_base"));

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

        textView_sensitivity = (TextView) findViewById(R.id.textView_sensitivity);
        seekBar_sensitivity = (SeekBar) findViewById(R.id.seekBar_sensitivity);
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
        setSensitivityUI(Util.getDefPref(this).getInt(Conf.sensitivityLevel, 10), true);
        initNoises();

        // 初始化录音机，用于检测噪音音量
        mRecorder = new Recorder();
    }

    // 选择音效组
    private void chooseNoiseSet() {
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
    }

    // 更新界面与播放音效
    public void updateUI_playNoise() {
        double dB;
        if (PREFERENCE_ENABLE_DB_MODE) {
            double referenceAmp = PREFERENCE_REFERENCE_AMP;
            dB = mRecorder.powerDb(referenceAmp);
        } else {
            dB = mRecorder.getAmplitudeEMA();
        }


        String show = String.format("%.0f", dB);

        long dBLong = (long) dB;
        if (dBLong >= sensitivity[sensitivityLevel]) {
            setSensitivityUI(sensitivity.length - 1, false);

            // 播放一次
            if (!seekBar_sensitivity.isEnabled())
                noiseList.get(noiseListId).getSounds().play();

            // 播放停止器
            mDelayTimer = new Timer();
            mDelayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        setSensitivityUI(Util.getDefPref(TroubleMakerActivity.this).getInt(Conf.sensitivityLevel, 1), true);
                    });
                }
            }, INTERVAL_DELAY); //delay 后启动一次
            textView_status_playing.setText(show);
            textView_status_playing.setTextColor(Color.RED);
        } else {
            if (seekBar_sensitivity.isEnabled()) {
                textView_status_playing.setText(show);
                textView_status_playing.setTextColor(Color.GREEN);
            }
        }
    }

    // 设置灵敏度调整控件的界面更新
    private void setSensitivityUI(int i, boolean enableSeekBar) {
        sensitivityLevel = i;
        textView_sensitivity.setText(sensitivity[sensitivityLevel] + "");
        seekBar_sensitivity.setProgress(i);
        seekBar_sensitivity.setEnabled(enableSeekBar);
        if (enableSeekBar && !(sensitivityLevel == sensitivity.length - 1)) {
            Util.getDefPref(TroubleMakerActivity.this).edit()
                    .putInt(Conf.sensitivityLevel, sensitivityLevel).apply();
        } else if (sensitivityLevel == sensitivity.length - 1) {
            textView_sensitivity.setText("MAX");
        }
    }

    // 获取噪音音量
    private void getNoiseLevel() {
        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(INTERVAL_RECORD);
                        } catch (InterruptedException e) {
                        }
                        runOnUiThread(() -> {
                            updateUI_playNoise();
                        });

                    }
                }
            };
            runner.start();
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
            mRecorder.startRecorder();
            getNoiseLevel();
        }

        // 加载一些设置
        PREFERENCE_ENABLE_DB_MODE = Util.getDefPref(this).getBoolean(Conf.pTmEnDbMode, true);
        PREFERENCE_REFERENCE_AMP = Double.parseDouble(Util.getDefPref(this).getString(Conf.pTmEnReferenceAmp, "0.00911881965"));
        if (PREFERENCE_REFERENCE_AMP == 0.00911881965) {
            PREFERENCE_REFERENCE_AMP = 10 * Math.exp(-7);
        }
        long avg;
        if (PREFERENCE_ENABLE_DB_MODE) {
            long MAX_DB = Long.parseLong(Util.getDefPref(this).getString(Conf.pTmMaxDb, "200"));
            avg = MAX_DB / (seekBarMax - 2);
        } else {
            avg = 500;
        }
        for (int i = 0; i < sensitivity.length - 1; i++) {
            sensitivity[i] = avg * i;
        }
        sensitivity[sensitivity.length - 1] = 999999999;
        setSensitivityUI(sensitivityLevel, true);
        INTERVAL_DELAY = Long.parseLong(Util.getDefPref(this).getString(Conf.pTmIntervalDelay, "6500"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecorder.stopRecorder();
        for (Noise n : noiseList) {
            n.stopAll();
        }
    }

    // requestPermissions() 回调，判断是否授予了权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRecorder.startRecorder();
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
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_stop:
                if (mDelayTimer != null) {
                    mDelayTimer.cancel();
                    mDelayTimer.purge();
                }
                setSensitivityUI(sensitivity.length - 1, true);
                break;
            case R.id.menu_stop_timer:
                // 定时停止播放
                intent = new Intent(TroubleMakerActivity.this, SetTimeActivity.class);
                startActivityForResult(intent, INTENT_STOP_TIME);
                break;
            case R.id.menu_choose_noise_set:
                chooseNoiseSet();
                break;
            case R.id.menu_settings:
                intent = new Intent(TroubleMakerActivity.this, TroubleMakerSettingsActivity.class);
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
