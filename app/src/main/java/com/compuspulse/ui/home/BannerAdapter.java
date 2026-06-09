package com.compuspulse.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.compuspulse.R;
import com.compuspulse.data.remote.model.Banner;

import java.util.List;

public class BannerAdapter extends PagerAdapter {

    private final Context context;
    private final List<Banner> bannerList;
    private final OnBannerClickListener listener;

    public BannerAdapter(Context context, List<Banner> bannerList, OnBannerClickListener listener) {
        this.context = context;
        this.bannerList = bannerList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return bannerList == null ? 0 : bannerList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, container, false);
        ImageView imageView = view.findViewById(R.id.iv_banner);

        Banner banner = bannerList.get(position);
        Glide.with(context)
                .load(banner.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imageView);

        view.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBannerClick(banner);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public interface OnBannerClickListener {
        void onBannerClick(Banner banner);
    }
}
