package com.canwdev.noise.noise;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.canwdev.noise.R;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.NestedListView;
import com.canwdev.noise.util.Util;

public class DetailViewActivity extends AppCompatActivity {
    private Noise noise;
    private Animation animCover;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        if (getIntent() != null) {
            collapsingToolbar.setTitle(getIntent().getStringExtra("name"));
            toolbar.setSubtitle("Shared Element Transitions");
        }
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);


        // Let the logic of click navigation arrow the same as back key , or has not the animation
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        FloatingActionButton fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        ImageView cover = (ImageView) findViewById(R.id.imageView_cover);
        animCover = AnimationUtils.loadAnimation(this, R.anim.anim_image_zoom_click);

        Intent intent = getIntent();

        if (intent != null) {
            if (getIntent().getBooleanExtra("coverBmp", false)) {
                // 将bitmap转换成byte传输，防止大图片导致的内存溢出
                byte[] bis = intent.getByteArrayExtra("cover");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
                cover.setImageBitmap(bitmap);
            } else {
                cover.setImageResource(intent.getIntExtra("cover", 0));
            }
            noise = intent.getParcelableExtra("noise");
            if (!noise.isAudio()) {
                noise.setLoaded(false);
                noise.loadSoundPool(this);
            } else {
                ProgressDialog progressDialog = ProgressDialog.show(this, "Loading",
                        "Please wait...", true);
                new Thread(() -> {
                    noise.initAudio(DetailViewActivity.this);
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                    });
                }).start();
            }
            //noise.getSounds().playLoop();
        }

        if (!noise.isAudio()) {
            if (Util.getDefPref(this).getBoolean(Conf.pEnTouch, false)) {
                cover.setOnTouchListener((v, event) -> {
                    noise.getSounds().play();
                    return false;
                });
            }
            fab_play.setOnClickListener(e -> {
                cover.startAnimation(animCover);
                noise.getSounds().play();
            });
            NestedListView listView = (NestedListView) findViewById(R.id.listView_sounds);
            String[] sounds = noise.getSounds().getFilenames();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, sounds);

            listView.setAdapter(adapter);

            listView.setOnItemClickListener((parent, view, position, id) -> {
                noise.getSounds().playById(position + 1);
            });

            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                noise.getSounds().playByIdLoop(position + 1);
                Snackbar.make(listView, R.string.toast_cycleing
                        , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                return true;
            });
        } else {
            fab_play.setVisibility(View.GONE);
            NestedListView listView = (NestedListView) findViewById(R.id.listView_sounds);
            String[] names = noise.getAudioFileNames();

            String[] finalNames = new String[names.length];
            for (int i = 0; i < names.length; i++) {
                finalNames[i] = names[i].substring(names[i].lastIndexOf("/") + 1);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, finalNames);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                if (noise.getAudio(position).isPlaying()) {
                    noise.getAudio(position).pause();
                } else {
                    noise.getAudio(position).playLoop();
                }
            });
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                Util.getDefPref(DetailViewActivity.this).edit()
                        .putString(Conf.defaultBGM, finalNames[position]).apply();
                Snackbar.make(listView, R.string.toast_default_bgm_set
                        , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                return true;
            });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        noise.stopAll2();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            CollapsingToolbarLayout collapsing_toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
            collapsing_toolbar_layout.setExpandedTitleTextColor(ColorStateList.valueOf(Color.TRANSPARENT));
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
