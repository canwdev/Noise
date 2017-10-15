package com.canwdev.noise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolRandom;
import com.canwdev.noise.util.SoundPoolUtil;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, View.OnLongClickListener {

    SharedPreferences pref;
    private DrawerLayout drawer;
    private SoundPoolUtil spu_drawer;
    private SoundPoolRandom spr_box;
    private SoundPoolRandom spr_boom;
    private SoundPoolRandom spr_gun;
    private Timer playMore;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init the preferences data of Settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_settings, false);
        // load preferences
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        // 初始化MD组件
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (pref.getBoolean(Conf.pEnDrSound, true)) {
            drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    spu_drawer.play(1);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    spu_drawer.play(1);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    spu_drawer.play(2);
                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        View headerView = navigationView.getHeaderView(0);
        LinearLayout nav_header = (LinearLayout) headerView.findViewById(R.id.nav_header);
        nav_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        spu_drawer = SoundPoolUtil.getInstance(MainActivity.this);
        initSoundPool();
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        button1.setOnClickListener(this);
        button1.setOnLongClickListener(this);
        button2.setOnClickListener(this);
        button2.setOnLongClickListener(this);
        button3.setOnClickListener(this);
        button3.setOnLongClickListener(this);
        button4.setOnClickListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent();

        switch (item.getItemId()) {

            case R.id.nav_settings:
                intent.setClass(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_about:
                intent.setClass(this, AboutActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }


        return true;
    }

    @Override
    public void onBackPressed() {
        // 判断是否打开了抽屉，打开了则关闭，否则退出
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                spr_box.play();
                break;
            case R.id.button2:
                spr_boom.play();
                break;
            case R.id.button3:
                spr_gun.play();
                break;
            case R.id.button4:
                releaseSoundPool();
                initSoundPool();
                if (playMore != null) {
                    Toast.makeText(this, "循环播放停止", Toast.LENGTH_SHORT).show();
                    playMore.cancel();
                    playMore.purge();
                    playMore = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                playEndless(spr_box);
                break;
            case R.id.button2:
                playEndless(spr_boom);
                break;
            case R.id.button3:
                playEndless(spr_gun);
                break;
            case R.id.button4:

                break;
            default:
                break;
        }
        return true;
    }

    private void initSoundPool() {
        spr_box = new SoundPoolRandom(MainActivity.this, "audio_box", 8);
        spr_boom = new SoundPoolRandom(MainActivity.this, "audio_boom", 33);
        spr_gun = new SoundPoolRandom(MainActivity.this, "audio_gun", 11);
    }

    private void releaseSoundPool() {
        spr_box.release();
        spr_boom.release();
        spr_gun.release();
    }

    private void playEndless(final SoundPoolRandom spr) {
        /*if (playMore != null) {
            Toast.makeText(this, spr.getFolderInfo() + " 循环播放停止", Toast.LENGTH_SHORT).show();
            playMore.cancel();
            playMore.purge();
            playMore = null;
        } else {*/
            Toast.makeText(this, spr.getFolderInfo() + " 循环播放中", Toast.LENGTH_SHORT).show();
            long interval = Integer.parseInt(pref.getString(Conf.pAuPlInterval, "500"));
            playMore = new Timer();
            playMore.schedule(new TimerTask() {
                @Override
                public void run() {
                    spr.play();
                }
            }, 0, interval);
    }



    /*private final class PlayMoreTask extends TimerTask {
        @Override
        public void run() {
            spr_gun.play();
            spr_box.play();
            spr_boom.play();
        }
    }*/
}
