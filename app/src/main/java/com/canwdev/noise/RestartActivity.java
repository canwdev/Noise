package com.canwdev.noise;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.canwdev.noise.service.BackgroundService;

// 通过启动这个Activity再执行System.exit(0)来重启App的所有Activity
// 但服务仍需手动停止
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
