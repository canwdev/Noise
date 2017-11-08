package com.canwdev.noise;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.canwdev.noise.util.AudioRecordDemo;

public class TroubleMakerActivity extends AppCompatActivity {

    String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trouble_maker);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 6.0+ 运行时权限检查
        if (ContextCompat.checkSelfPermission(
                this, PERMISSION_RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this
                    , new String[]{PERMISSION_RECORD_AUDIO}, 1);
        } else {
            AudioRecordDemo ard = new AudioRecordDemo();
            ard.getNoiseLevel();
        }
    }

    // requestPermissions() 回调，判断是否授予了权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AudioRecordDemo ard = new AudioRecordDemo();
                    ard.getNoiseLevel();
                } else {
                    Toast.makeText(this, "Permission denied: \n" + PERMISSION_RECORD_AUDIO
                            , Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}
