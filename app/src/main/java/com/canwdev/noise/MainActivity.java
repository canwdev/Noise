package com.canwdev.noise;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.canwdev.noise.noise.Noise;
import com.canwdev.noise.noise.NoiseAdapter;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

// TODO 写备注
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Main##";
    SharedPreferences pref;
    private DrawerLayout drawer;
    private SoundPoolUtil spu_drawer;
    private Timer stopTimer;

    private List<Noise> noiseList = new ArrayList<>();

    private void initNoises() {
        noiseList.add(new Noise(R.drawable.ic_shuffle_yellow_500_24dp, "inst/piano"));

        noiseList.add(new Noise(R.drawable.ra_box, "ra2/audio_box"));
        noiseList.add(new Noise(R.drawable.ra_boom, "ra2/audio_boom"));
        noiseList.add(new Noise(R.drawable.ra_gun, "ra2/audio_gun"));
        noiseList.add(new Noise(R.drawable.ra_allied_base, "ra2/audio_base"));

        noiseList.add(new Noise(R.drawable.gc_yuanshou, "guichu/yuanshou"));
        noiseList.add(new Noise(R.drawable.gc_gboy, "guichu/gboy"));
        noiseList.add(new Noise(R.drawable.gc_shengdiyage, "guichu/shengdiyage"));
        noiseList.add(new Noise(R.drawable.gc_liangyifeng, "guichu/liangyifeng"));

        noiseList.add(new Noise(R.drawable.gc_liangfeifan, "guichu/liangfeifan"));
        noiseList.add(new Noise(R.drawable.gc_zhexue, "guichu/zhexue"));
        noiseList.add(new Noise(R.drawable.gc_haa, "guichu/haa"));
        noiseList.add(new Noise(R.drawable.gc_other, "guichu/other"));
    }

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
                //TODO: BGM
            }
        });

        spu_drawer = SoundPoolUtil.getInstance(MainActivity.this);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);
        NoiseAdapter adapter = new NoiseAdapter(noiseList);
        recyclerView.setAdapter(adapter);


        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("烧酒祈祷中...");
        dialog.setCancelable(false);
        dialog.show();
        new Thread((Runnable) () -> {
            initNoises();
            dialog.dismiss();
        }
        ).start();
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

            case R.id.nav_reset:
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("烧酒祈祷中...");
                dialog.setCancelable(false);
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Noise n : noiseList) {
                            n.stopAll();
                        }
                        noiseList.clear();
                        initNoises();
                        dialog.dismiss();
                    }
                }).start();
                break;

            case R.id.nav_fc:
                System.exit(0);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                Toast.makeText(this, "循环播放停止", Toast.LENGTH_SHORT).show();
                for (Noise n : noiseList) {
                    n.stopAll();
                }
                break;
            case R.id.menu_stop_timer:
                final Calendar calendar = Calendar.getInstance();
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {

                        calendar.set(Calendar.HOUR_OF_DAY, i);
                        calendar.set(Calendar.MINUTE, i1);
                        long timer = calendar.getTime().getTime();
                        Log.d(TAG, "timer: " + timer);
                        long now = new Date().getTime();
                        Log.d(TAG, "now--: " + now);
                        if (now < timer) {
                            long diff = timer - now;
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                            String hms = sdf.format(diff);

                            Toast.makeText(MainActivity.this, "将在以下时间后停止: " + hms, Toast.LENGTH_SHORT).show();
                            stopTimer = new Timer();
                            stopTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    for (Noise n : noiseList) {
                                        n.stopAll();
                                    }
                                    Looper.prepare();
                                    Toast.makeText(MainActivity.this, "循环播放停止", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                    /*finish();
                                    System.exit(0);*/
                                }
                            }, diff); //diff秒后启动一次

                        } else {
                            Toast.makeText(MainActivity.this, "选择的时间无效", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
                break;
            default:
                break;

        }
        return true;
    }


}
