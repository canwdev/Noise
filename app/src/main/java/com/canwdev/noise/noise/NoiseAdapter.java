package com.canwdev.noise.noise;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public NoiseAdapter(List<Noise> mNoiseList) {
        this.mNoiseList = mNoiseList;
    }

    @Override
    public NoiseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_grid, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        // 设置点击事件
        holder.view.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Noise noise = mNoiseList.get(position);
            noise.loadSoundPool(view.getContext());
            noise.getSounds().play();
        });

        // 设置长按事件
        holder.view.setOnLongClickListener(v -> {
            int position = holder.getAdapterPosition();
            Noise noise = mNoiseList.get(position);
            noise.loadSoundPool(view.getContext());
            noise.getSounds().endlessPlay();
            return true;
        });

        // 设置触摸事件
        if (Util.getDefPref(view.getContext()).getBoolean(Conf.pEnTouch, false)) {
            holder.view.setOnTouchListener((v, event) -> {
                int position = holder.getAdapterPosition();
                Noise noise = mNoiseList.get(position);
                noise.loadSoundPool(view.getContext());
                noise.getSounds().play();
                return true;
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView_cover;
        TextView textView_name;
        Noise.SoundPoolRandom spl;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView_cover = (ImageView) itemView.findViewById(R.id.image_noise);
            textView_name = (TextView) itemView.findViewById(R.id.textView_name);
        }

    }


}
