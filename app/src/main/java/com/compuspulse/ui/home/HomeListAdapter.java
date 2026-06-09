package com.compuspulse.ui.home;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.compuspulse.R;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.Banner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeListAdapter extends ListAdapter<HomeListItem, RecyclerView.ViewHolder> {

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_NO_MORE = 2;
    public static final int STATUS_LOAD_FAIL = 3;
    private static final long NO_OPENED_ITEM_ID = -3L;

    private final Context context;
    private final OnArticleClickListener onArticleClickListener;
    private final OnArticleFavoriteClickListener onArticleFavoriteClickListener;
    private final int swipeActionWidthPx;
    private long openedItemId = NO_OPENED_ITEM_ID;

    public HomeListAdapter(Context context,
                           OnArticleClickListener onArticleClickListener,
                           OnArticleFavoriteClickListener onArticleFavoriteClickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.onArticleClickListener = onArticleClickListener;
        this.onArticleFavoriteClickListener = onArticleFavoriteClickListener;
        this.swipeActionWidthPx = dpToPx(context, 56);
        setHasStableIds(true);
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    public void submitData(List<Banner> banners, List<Article> articles, int loadMoreStatus) {
        List<HomeListItem> items = new ArrayList<>();
        //将外部的banner、article、loadMoreStatus转换为HomeListItem，放入item列表中
        items.add(HomeListItem.banner(banners == null ? Collections.emptyList() : new ArrayList<>(banners)));

        if (articles != null) {
            for (Article article : articles) {
                items.add(HomeListItem.article(article));
            }
        }

        items.add(HomeListItem.footer(loadMoreStatus));
        submitList(items);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getStableId();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    public boolean isArticlePosition(int position) {
        if (position < 0 || position >= getItemCount()) {
            return false;
        }
        return getItem(position).getType() == HomeListItem.TYPE_ARTICLE;
    }

    @Nullable
    public Article getArticleAt(int position) {
        if (!isArticlePosition(position)) {
            return null;
        }
        return getItem(position).getArticle();
    }

    public boolean isSwipeOpen(int position) {
        if (!isArticlePosition(position)) {
            return false;
        }
        return getItem(position).getStableId() == openedItemId;
    }

    public boolean hasOpenedSwipe() {
        return openedItemId != NO_OPENED_ITEM_ID;
    }

    public void openSwipeAt(int position) {
        if (!isArticlePosition(position)) {
            notifyItemChanged(position);
            return;
        }

        long targetId = getItem(position).getStableId();
        int previousPosition = findPositionByStableId(openedItemId);
        openedItemId = targetId;

        if (previousPosition != RecyclerView.NO_POSITION && previousPosition != position) {
            notifyItemChanged(previousPosition);
        }
        notifyItemChanged(position);
    }

    public void closeSwipeAt(int position) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }
        if (isSwipeOpen(position)) {
            openedItemId = NO_OPENED_ITEM_ID;
        }
        notifyItemChanged(position);
    }

    public void closeSwipe() {
        int previousPosition = findPositionByStableId(openedItemId);
        openedItemId = NO_OPENED_ITEM_ID;
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from (context);
        if (viewType == HomeListItem.TYPE_BANNER) {
            return new BannerViewHolder(inflater.inflate(R.layout.item_home_banner_container, parent, false));
        }
        if (viewType == HomeListItem.TYPE_FOOTER) {
            return new FooterViewHolder(inflater.inflate(R.layout.item_home_footer, parent, false));
        }
        return new ArticleViewHolder(inflater.inflate(R.layout.item_article, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeListItem item = getItem(position);
        if (holder instanceof BannerViewHolder) {
            bindBanner((BannerViewHolder) holder, item.getBanners());
        } else if (holder instanceof FooterViewHolder) {
            bindFooter((FooterViewHolder) holder, item.getFooterStatus());
        } else if (holder instanceof ArticleViewHolder) {
            bindArticle((ArticleViewHolder) holder, item.getArticle(), position);
        }
    }

    private void bindBanner(BannerViewHolder holder, List<Banner> banners) {
        BannerAdapter bannerAdapter = new BannerAdapter(context, banners, banner -> {
            if (onArticleClickListener != null && banner != null) {
                Article article = new Article();
                article.setId(banner.getId());
                article.setTitle(banner.getTitle());
                article.setSummary("来自首页轮播图的推荐内容");
                article.setSource("WanAndroid Banner");
                article.setSourceUrl(banner.getTargetUrl());
                article.setPublishTime("推荐内容");
                onArticleClickListener.onArticleClick(article);
            }
        });
        holder.viewPager.setAdapter(bannerAdapter);
    }

    private void bindArticle(ArticleViewHolder holder, Article article, int position) {
        if (article == null) {
            return;
        }
        //通过平移文章的容器和按钮的容器实现滑动打开侧边栏
        boolean swipeOpen = isSwipeOpen(position);
        holder.contentContainer.setTranslationX(swipeOpen ? -swipeActionWidthPx : 0f);
        holder.favoriteAction.setImageResource(
                article.isFavorite() ? R.drawable.ic_favorite_selected : R.drawable.ic_favorite_unselected
        );
        holder.favoriteActionContainer.setOnClickListener(v -> {
            if (onArticleFavoriteClickListener != null) {
                onArticleFavoriteClickListener.onArticleFavoriteClick(article);
            }
            closeSwipe();
        });

        holder.tvTitle.setText(article.getTitle());
        holder.tvSummary.setText(article.getSummary());
        holder.tvSource.setText(article.getSource());
        holder.tvTime.setText(article.getPublishTime());
        holder.tvCommentCount.setText(article.getCommentCount() > 0
                ? "热度 " + article.getCommentCount()
                : article.getCategory());

        if (TextUtils.isEmpty(article.getImageUrl())) {
            holder.ivCover.setVisibility(View.GONE);
        } else {
            holder.ivCover.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onArticleClickListener != null) {
                onArticleClickListener.onArticleClick(article);
            }
        });
    }

    private void bindFooter(FooterViewHolder holder, int loadMoreStatus) {
        if (loadMoreStatus == STATUS_LOADING) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("正在加载...");
        } else if (loadMoreStatus == STATUS_NO_MORE) {
            holder.progressBar.setVisibility(View.GONE);
            holder.tvStatus.setText("没有更多内容了");
        } else if (loadMoreStatus == STATUS_LOAD_FAIL) {
            holder.progressBar.setVisibility(View.GONE);
            holder.tvStatus.setText("加载失败，继续上拉重试");
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.tvStatus.setText("上拉加载更多");
        }
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ViewPager viewPager;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.homepage_ViewPager);
        }
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout contentContainer;
        private final FrameLayout favoriteActionContainer;
        private final ImageView favoriteAction;
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvSummary;
        private final TextView tvSource;
        private final TextView tvTime;
        private final TextView tvCommentCount;

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            contentContainer = itemView.findViewById(R.id.article_content_container);
            favoriteActionContainer = itemView.findViewById(R.id.swipe_action_favorite_container);
            favoriteAction = itemView.findViewById(R.id.iv_swipe_favorite);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        private final ProgressBar progressBar;
        private final TextView tvStatus;

        FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_loading);
            tvStatus = itemView.findViewById(R.id.tv_footer_status);
        }
    }

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }

    public interface OnArticleFavoriteClickListener {
        void onArticleFavoriteClick(Article article);
    }

    private int findPositionByStableId(long stableId) {
        if (stableId == NO_OPENED_ITEM_ID) {
            return RecyclerView.NO_POSITION;
        }
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).getStableId() == stableId) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
    //dp转换为像素值确保各设备一致
    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private static final DiffUtil.ItemCallback<HomeListItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<HomeListItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull HomeListItem oldItem, @NonNull HomeListItem newItem) {
                    return oldItem.getType() == newItem.getType()
                            && oldItem.getStableId() == newItem.getStableId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull HomeListItem oldItem, @NonNull HomeListItem newItem) {
                    return oldItem.isContentSame(newItem);
                }
            };
}
