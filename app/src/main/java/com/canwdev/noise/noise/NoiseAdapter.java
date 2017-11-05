package com.canwdev.noise.noise;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.canwdev.noise.R;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

import java.util.List;

/**
 * Created by CAN on 2017/10/15.
 * 用于适配RecyclerView的适配器
 */

public class NoiseAdapter extends RecyclerView.Adapter<NoiseAdapter.ViewHolder> {
    private List<Noise> mNoiseList;
    private Context mContext;
    private View mParentView;

    public NoiseAdapter(Context mContext, List<Noise> mNoiseList) {
        this.mContext = mContext;
        this.mNoiseList = mNoiseList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mParentView = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_grid, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        // 设置点击事件
        holder.view.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Noise noise = mNoiseList.get(position);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && Util.getDefPref(view.getContext()).getBoolean(Conf.pEnShareView, false)) {

                Intent intent = new Intent(mContext, DetailViewActivity.class);
                intent.putExtra("name", noise.getName());
                Bitmap cover = noise.getImageBmp();
                if (cover != null) {
                    intent.putExtra("coverBmp", true);
                    intent.putExtra("cover", cover);
                } else {
                    intent.putExtra("coverBmp", false);
                    intent.putExtra("cover", noise.getImageId());
                }

                intent.putExtra("noise", noise);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mContext.startActivity(intent);
//                    mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation
//                            ((Activity) mContext, holder.imageView_cover, "shareView").toBundle());
                } else {
                    mContext.startActivity(intent);
                }
            } else {
                noise.loadSoundPool(view.getContext());
                noise.getSounds().play();
            }
        });

        // 设置长按事件
        // [主要]无尽随机播放，若需停止，使用 noise.stopAll();
        holder.view.setOnLongClickListener(v -> {
            int position = holder.getAdapterPosition();
            Noise noise = mNoiseList.get(position);
            noise.loadSoundPool(view.getContext());

            int count = noise.getSounds().getMaxSoundCount();
            String name = noise.getName();

            if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnAdvancedInterval, false)) {
                if (!noise.getSounds().isEndlessPlaying()) {
                    noise.getSounds().advancedEndlessPlay();
                    Snackbar.make(mParentView, name + " 高级间隔播放中，共 " + count + " 个文件"
                            , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                } else {
                    Snackbar.make(mParentView, R.string.toast_adv_cycle_not_allowed
                            , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            } else {
                if (!noise.getSounds().isEndlessPlaying()) {
                    noise.getSounds().directEndlessPlay();
                    Snackbar.make(mParentView, name + " 循环播放中，共 " + count + " 个文件"
                            , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                } else if (Util.getDefPref(mContext).getBoolean(Conf.pAuEnMultiLoop, true)) {
                    noise.getSounds().directEndlessPlay();
                    Snackbar.make(mParentView, name + " 循环播放中，共 " + count + " 个文件"
                            , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                } else {
                    Snackbar.make(mParentView, R.string.toast_cycle_not_allowed
                            , Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
            }

            return true;
        });

        // 设置触摸事件
        if (Util.getDefPref(view.getContext()).getBoolean(Conf.pEnTouch, false)) {
            holder.view.setOnTouchListener((v, event) -> {
                int position = holder.getAdapterPosition();
                Noise noise = mNoiseList.get(position);
                noise.loadSoundPool(view.getContext());
                noise.getSounds().play();
                return false;
            });
        }
        return holder;
    }

    /**
     * 初始化外观
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(NoiseAdapter.ViewHolder holder, int position) {
        final NoiseAdapter.ViewHolder viewHolder = holder;

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_recycler_item_show);
        viewHolder.imageView_cover.startAnimation(animation);
        viewHolder.textView_name.startAnimation(animation);

        Noise noise = mNoiseList.get(position);
        Bitmap cover = noise.getImageBmp();

        if (cover != null) {
            holder.imageView_cover.setImageBitmap(cover);
        } else {
            holder.imageView_cover.setImageResource(noise.getImageId());
        }

        if (!noise.getName().isEmpty()) {
            holder.textView_name.setText(noise.getName());
        } else {
            holder.textView_name.setVisibility(View.INVISIBLE);
        }
        holder.spl = noise.getSounds();
    }

    @Override
    public int getItemCount() {
        return mNoiseList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView_cover;
        TextView textView_name;
        SoundPoolRandom spl;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView_cover = itemView.findViewById(R.id.image_noise);
            textView_name = itemView.findViewById(R.id.textView_name);
        }

    }


}
