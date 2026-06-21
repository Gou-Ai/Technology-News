package com.compuspulse.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.compuspulse.R
import com.compuspulse.data.remote.model.Banner

class BannerAdapter(
    private val context: Context,
    private val bannerList: List<Banner>?,
    private val listener: OnBannerClickListener?
) : PagerAdapter() {

    override fun getCount(): Int = bannerList?.size ?: 0

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.item_banner, container, false)
        val imageView: ImageView = view.findViewById(R.id.iv_banner)

        val banner = bannerList!![position]
        Glide.with(context)
            .load(banner.getImageUrl())
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(imageView)

        view.setOnClickListener {
            listener?.onBannerClick(banner)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    fun interface OnBannerClickListener {
        fun onBannerClick(banner: Banner)
    }
}
