package com.canwdev.noise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.canwdev.noise.noise.Audio;
import com.canwdev.noise.noise.Noise;
import com.canwdev.noise.noise.NoiseAdapter;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.SoundPoolUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int INTENT_STOP_TIME = 1;
    private static final String TAG = "Main##";
    SharedPreferences pref;
    private DrawerLayout drawer;
    private SoundPoolUtil spu_drawer;
    private Audio bgm;
    private List<Noise> noiseList = new ArrayList<>();
    // 按2下返回键退出计时器
    private long doubleBackExitTime = 0;
    private SwipeRefreshLayout swipeRefresh;
    // 构建Runnable对象，在runnable中更新界面
    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            //更新界面
            swipeRefresh.setRefreshing(false);
        }

    };
    private Handler handler;

    // 初始化声音列表（此时SoundPool并未加载，将在第一次点击条目时加载）
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

        try {
            noiseList.add(new Noise(this,"testaudio",true));
            noiseList.add(new Noise(this,"bgm",true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 初始化界面音效与BGM
    private void initAudio() {
        // 初始化抽屉基本SoundPool，音频文件在/res/raw
        spu_drawer = SoundPoolUtil.getInstance(MainActivity.this);

        bgm = new Audio(this, "bgm/bgm_finally_animal_sister.mp3");
    }

    private void stopNoises() {
        for (Noise n : noiseList) {
            n.stopAll();
        }
        Snackbar.make(drawer, R.string.toast_cycle_stop
                , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
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
        //toolbar.setSubtitle(R.string.professional);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // 初始化界面音效与BGM
        initAudio();

        // 抽屉滑动时播放声音
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
        LinearLayout nav_header = headerView.findViewById(R.id.nav_header);
        nav_header.setOnClickListener(v -> {
            // 点击播放bgm，再点暂停
            if (bgm.isPlaying()) {
                bgm.pause();
                Snackbar.make(drawer, R.string.bgm_pause
                        , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            } else {
                bgm.playLoop();
                Snackbar.make(drawer, R.string.bgm_playing
                        , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }

        });

        /*int ori = this.getResources().getConfiguration().orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {*/
        // 禁止横屏
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidthDp = (int) (displayMetrics.widthPixels / displayMetrics.density);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        int spanCount;

        /*if (screenWidthDp >= 600) {
            spanCount = 6;
        } else {
            spanCount = 4;
        }*/
        spanCount = screenWidthDp / 100;
        if (spanCount < 4) spanCount = 4;

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(layoutManager);
        NoiseAdapter adapter = new NoiseAdapter(this, noiseList);
        recyclerView.setAdapter(adapter);

        // 下拉重置SoundPool
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(() -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(android.R.string.dialog_alert_title))
                    .setOnDismissListener(dialog1 -> swipeRefresh.setRefreshing(false))
                    .setMessage(getResources().getString(R.string.reset_soundpool) + "?")
                    .setNeutralButton(getString(R.string.restart_app), (dialogInterface, i) -> {
                        startActivity(new Intent(this, RestartActivity.class));
                    })
                    .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> swipeRefresh.setRefreshing(false))
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> resetSoundPool());
            dialog.show();
        });

        initNoises();

        //创建属于主线程的handler
        handler = new Handler();
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
                resetSoundPool();
                break;

            case R.id.nav_fc:
                // 强行停止
                stopNoises();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void resetSoundPool() {
        // 重置列表中的所有SoundPool
        // TODO: 2017/11/1 Fix errors
        // E/AudioTrack: AudioFlinger could not create track, status: -12
        // E/SoundPool: Error creating AudioTrack
        swipeRefresh.setRefreshing(true);

        new Thread(() -> {
            stopNoises();
            noiseList.clear();
            initNoises();
            handler.post(runnableUi);
            Snackbar.make(drawer, getString(R.string.reset_soundpool) + "\n" + getString(R.string.toast_cycle_stop)
                    , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }).start();
    }

    @Override
    public void onBackPressed() {
        // 判断是否打开了抽屉，打开了则关闭，否则退出
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // 智能的再按一次退出
            boolean isPlaying = false;
            for (Noise n : noiseList) {
                if (n.isLoaded() && n.getSounds().isEndlessPlaying()) {
                    isPlaying = true;
                    break;
                }
            }
            if (isPlaying && (System.currentTimeMillis() - doubleBackExitTime) > 2000) {
                Snackbar.make(drawer, R.string.toast_press_again_to_exit
                        , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                doubleBackExitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
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
                // 随机播放一段时间
                int times = 100;
                long interval = 50;

                new Thread(() -> {
                    for (int i = 0; i < times; i++) {
                        try {
                            Random random = new Random();
                            Noise noise = noiseList.get(random.nextInt(noiseList.size()));
                            noise.loadSoundPool(MainActivity.this);
                            if (noise.isLoaded()) {
                                noise.getSounds().play();
                            }
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                break;
            case R.id.menu_reset:
                stopNoises();
                break;
            case R.id.menu_stop_timer:
                // 定时停止播放
                Intent intent = new Intent(MainActivity.this, SetTimeActivity.class);
                startActivityForResult(intent, INTENT_STOP_TIME);

                break;
            default:
                break;

        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNoises();
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
                    // 若不设置TimeZone，则会多出8小时（中国+8区）
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                    String hms = sdf.format(millisecond);

                    Log.d(TAG, "onActivityResult: " + hms + " " + autoExit);

                    if (millisecond > 0) {
                        Toast.makeText(MainActivity.this, "将在 " + hms + " 后停止", Toast.LENGTH_LONG).show();
                        Timer stopTimer = new Timer();
                        stopTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                stopNoises();
                                Looper.prepare();
                                if (autoExit) {
                                    stopNoises();
                                    System.exit(0);
                                }
                                Looper.loop();

                            }
                        }, millisecond); //millisecond 后启动一次
                    } else {
                        Toast.makeText(MainActivity.this, R.string.toast_invalid_time, Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            default:
        }
    }
}
