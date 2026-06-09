package com.compuspulse.ui.category;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.compuspulse.R;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.ui.common.ArticleDiffCallback;

public class CategoryArticleAdapter extends ListAdapter<Article, CategoryArticleAdapter.ViewHolder> {

    private final OnCategoryArticleClickListener listener;

    public CategoryArticleAdapter(OnCategoryArticleClickListener listener) {
        super(new ArticleDiffCallback());
        this.listener = listener;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    @NonNull
    @Override
    public CategoryArticleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryArticleAdapter.ViewHolder holder, int position) {
        Article article = getItem(position);

        holder.tvTitle.setText(article.getTitle());
        holder.tvSummary.setText(article.getSummary());
        holder.tvSource.setText(article.getSource() + " · " + article.getCategory());
        holder.tvTime.setText(article.getPublishTime());

        if (TextUtils.isEmpty(article.getImageUrl())) {
            holder.ivCover.setVisibility(View.GONE);
        } else {
            holder.ivCover.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArticleClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvSummary;
        TextView tvSource;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    public interface OnCategoryArticleClickListener {
        void onArticleClick(Article article);
    }
}
