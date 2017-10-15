package com.canwdev.noise.noise;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.canwdev.noise.R;
import com.canwdev.noise.util.Conf;
import com.canwdev.noise.util.Util;

import java.util.List;

/**
 * Created by CAN on 2017/10/15.
 */

public class NoiseAdapter extends RecyclerView.Adapter<NoiseAdapter.ViewHolder>{
    private List<Noise> mNoiseList;

    public NoiseAdapter(List<Noise> mNoiseList) {
        this.mNoiseList = mNoiseList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView cover;
        SoundPoolRandom spl;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            cover = (ImageView) itemView.findViewById(R.id.image_noise);
        }

    }

    @Override
    public NoiseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_grid, parent, false);

        // 设置点击事件
        final ViewHolder holder = new ViewHolder(view);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Noise noise = mNoiseList.get(position);
                noise.getSounds().play();
            }
        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                Noise noise = mNoiseList.get(position);
                noise.getSounds().endlessPlay();
                return true;
            }
        });

        if (Util.getDefPref(view.getContext()).getBoolean(Conf.pEnTouch, false)) {
            holder.view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int position = holder.getAdapterPosition();
                    Noise noise = mNoiseList.get(position);
                    noise.getSounds().play();
                    return true;
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(NoiseAdapter.ViewHolder holder, int position) {
        Noise noise = mNoiseList.get(position);
        holder.cover.setImageResource(noise.getImageId());
        holder.spl = noise.getSounds();
    }

    @Override
    public int getItemCount() {
        return mNoiseList.size();
    }


}
