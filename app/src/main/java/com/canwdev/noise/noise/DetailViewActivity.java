package com.canwdev.noise.noise;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.canwdev.noise.R;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

public class DetailViewActivity extends AppCompatActivity {
    private Noise noise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        if (getIntent() != null) {
            collapsingToolbar.setTitle(getIntent().getStringExtra("name"));
        }
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);


        // Let the logic of click navigation arrow the same as back key , or has not the animation
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        FloatingActionButton fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        ImageView cover = (ImageView) findViewById(R.id.imageView_cover);

        Intent intent = getIntent();

        if (intent != null) {
            if (getIntent().getBooleanExtra("coverBmp", false)) {
                cover.setImageBitmap(intent.getParcelableExtra("cover"));
            } else {
                cover.setImageResource(intent.getIntExtra("cover", 0));
            }
            noise = (Noise) intent.getParcelableExtra("noise");
            noise.setLoaded(false);
            noise.loadSoundPool(this);
            //noise.getSounds().play();
        }

        // 触摸时的动画，来自　https://github.com/Eajy/MaterialDesignDemo
        fab_play.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator upAnim = ObjectAnimator.ofFloat(view, "translationZ", 0);
                    upAnim.setDuration(200);
                    upAnim.setInterpolator(new DecelerateInterpolator());
                    upAnim.start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator downAnim = ObjectAnimator.ofFloat(view, "translationZ", 10);
                    downAnim.setDuration(200);
                    downAnim.setInterpolator(new AccelerateInterpolator());
                    downAnim.start();
                    break;
            }
            return false;
        });

        if (Util.getDefPref(this).getBoolean(Conf.pEnTouch, false)) {
            collapsingToolbar.setOnTouchListener((v, event) -> {
                noise.getSounds().play();
                return false;
            });
        }


        fab_play.setOnClickListener(e -> noise.getSounds().play());
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
