package com.canwdev.noise.noise;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_share_view);
        if (getIntent() != null) {
            toolbar.setSubtitle(getIntent().getStringExtra("name"));
        } else {
            toolbar.setSubtitle("Shared Element Transitions");
        }
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);

        // Let the logic of click navigation arrow the same as back key , or has not the animation
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        CardView card_share_view = (CardView) findViewById(R.id.card_share_view);
        ImageView imageView_finish = (ImageView) findViewById(R.id.imageView_finish);
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
        card_share_view.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator upAnim = ObjectAnimator.ofFloat(view, "translationZ", 0);
                    upAnim.setDuration(200);
                    upAnim.setInterpolator(new DecelerateInterpolator());
                    upAnim.start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator downAnim = ObjectAnimator.ofFloat(view, "translationZ", 20);
                    downAnim.setDuration(200);
                    downAnim.setInterpolator(new AccelerateInterpolator());
                    downAnim.start();
                    break;
            }
            if (Util.getDefPref(view.getContext()).getBoolean(Conf.pEnTouch, false)) {
                noise.getSounds().play();
            }
            return false;
        });

        card_share_view.setOnClickListener(e-> noise.getSounds().play());

        Button button = (Button) findViewById(R.id.button);
        Button button2 = (Button) findViewById(R.id.button2);

        button.setOnClickListener(e->{
            noise.getSounds().directEndlessPlay();
        });

        button2.setOnClickListener(e->{
            noise.getSounds().advancedEndlessPlay();
        });

        imageView_finish.setOnClickListener(e-> onBackPressed());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        noise.stopAll2();
    }
}
