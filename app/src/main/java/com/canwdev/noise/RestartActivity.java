package com.canwdev.noise;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.canwdev.noise.R;
import com.canwdev.noise.noise.Noise;
import com.canwdev.noise.service.BackgroundService;

public class RestartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restart);

        // 停止BackgroundService服务
        Intent intent = new Intent(this, BackgroundService.class);
        stopService(intent);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        new Thread(() -> {
            int progress = 0;
            int adds = 15;
            while (progress <= 100) {
                progress += adds;
                if (adds > 1) {
                    --adds;
                }
                progressBar.setProgress(progress);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (progress >= 100) {
                System.exit(0);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }
}
