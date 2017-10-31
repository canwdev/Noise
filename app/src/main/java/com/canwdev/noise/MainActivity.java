package com.canwdev.noise;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.canwdev.noise.noise.Noise;
import com.canwdev.noise.noise.NoiseAdapter;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

// TODO 写备注
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int INTENT_STOP_TIME = 1;
    private static final String TAG = "Main##";
    SharedPreferences pref;
    private DrawerLayout drawer;
    private SoundPoolUtil spu_drawer;
    private Timer stopTimer;

    private List<Noise> noiseList = new ArrayList<>();

    private void initNoises() {

        noiseList.add(new Noise(R.drawable.ra_box, "ra2/audio_box"));
        noiseList.add(new Noise(R.drawable.ra_boom, "ra2/audio_boom"));
        noiseList.add(new Noise(R.drawable.ra_gun, "ra2/audio_gun"));
        noiseList.add(new Noise(R.drawable.ra_allied_base, "ra2/audio_base"));

        noiseList.add(new Noise(this, "guichu/yuanshou"));
        noiseList.add(new Noise(this, "guichu/gboy"));
        noiseList.add(new Noise(this,  "guichu/shengdiyage"));
        noiseList.add(new Noise(this,  "guichu/liangyifeng"));

        noiseList.add(new Noise(this,  "guichu/liangfeifan"));
        noiseList.add(new Noise(this,  "guichu/zhexue"));
        noiseList.add(new Noise(this,  "guichu/haa"));
        noiseList.add(new Noise(this,  "guichu/other"));

        noiseList.add(new Noise(this, "testres"));
        noiseList.add(new Noise(this, "inst/piano"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init the preferences data of Settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_settings, false);
        // loadSoundPool preferences
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

        initNoises();
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
                // TODO: 2017/10/24 播放多个条目后其他条目点击不能播放的奇怪问题
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.loading));
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
            case R.id.menu_random_play:
                // TODO: 2017/10/24 随机播放一段时间，同样使用SetTimeActivity
                break;
            case R.id.menu_reset:
                Toast.makeText(this, "循环播放停止", Toast.LENGTH_SHORT).show();
                for (Noise n : noiseList) {
                    n.stopAll();
                }
                break;
            case R.id.menu_stop_timer:
                Intent intent = new Intent(MainActivity.this, SetTimeActivity.class);
                startActivityForResult(intent, INTENT_STOP_TIME);

                break;
            default:
                break;

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_STOP_TIME:
                if (resultCode == RESULT_OK) {
                    long millisecond = data.getLongExtra("millisecond", -1);
                    boolean autoExit = data.getBooleanExtra("autoExit", true);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    // 若不设置TimeZone，则会多出8小时（中国+8区）
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    String hms = sdf.format(millisecond);

                    Log.d(TAG, "onActivityResult: " + hms + " " + autoExit);

                    if (millisecond > 0) {
                        Toast.makeText(MainActivity.this, "将在 " + hms + " 后停止", Toast.LENGTH_SHORT).show();
                        stopTimer = new Timer();
                        stopTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                for (Noise n : noiseList) {
                                    n.stopAll();
                                }
                                Looper.prepare();
                                Toast.makeText(MainActivity.this, "循环播放停止", Toast.LENGTH_SHORT).show();
                                if (autoExit) System.exit(0);
                                Looper.loop();

                            }
                        }, millisecond); //millisecond 后启动一次
                    } else {
                        Toast.makeText(MainActivity.this, "时间无效", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            default:
        }
    }
}
