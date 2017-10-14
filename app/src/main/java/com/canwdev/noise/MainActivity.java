package com.canwdev.noise;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.canwdev.noise.util.PlayAssetsRandom;
import com.canwdev.noise.util.SoundPoolUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DrawerLayout drawer;
    private SoundPoolUtil spu;
    private PlayAssetsRandom par_box;
    private PlayAssetsRandom par_boom;
    private PlayAssetsRandom par_gun;
    private Timer playMore;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spu = SoundPoolUtil.getInstance(MainActivity.this);
        par_box = new PlayAssetsRandom(this, "audio_box", 8);
        par_boom = new PlayAssetsRandom(this, "audio_boom", 33);
        par_gun = new PlayAssetsRandom(this, "audio_gun", 11);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                spu.play(1);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                spu.play(1);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                spu.play(2);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        View headerView = navigationView.getHeaderView(0);
        LinearLayout nav_header = (LinearLayout) headerView.findViewById(R.id.nav_header);
        nav_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);*/
                /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);*/
            }
        });

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                playMore = new Timer();
                playMore.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        par_gun.play();
                    }
                }, 0, 500);
                return true;
            }
        });
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
                par_box.play();
                break;
            case R.id.button2:
                par_boom.play();
                break;
            case R.id.button3:
                par_gun.play();
                break;
            case R.id.button4:
                par_box.release();
                par_boom.release();
                par_gun.release();
                par_box = new PlayAssetsRandom(MainActivity.this, "audio_box", 8);
                par_boom = new PlayAssetsRandom(MainActivity.this, "audio_boom", 33);
                par_gun = new PlayAssetsRandom(MainActivity.this, "audio_gun", 11);
                playMore.cancel();
                break;
            default:break;
        }
    }

    /*private final class PlayMoreTask extends TimerTask {
        @Override
        public void run() {
            par_gun.play();
            par_box.play();
            par_boom.play();
        }
    }*/
}
