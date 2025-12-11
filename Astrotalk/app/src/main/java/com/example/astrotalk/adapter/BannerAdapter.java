package com.example.astrotalk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.astrotalk.R;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    // drawable ids of your banners
    private int[] banners;

    public BannerAdapter(int[] banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.imgBanner.setImageResource(banners[position]);
    }

    @Override
    public int getItemCount() {
        return banners.length;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}
