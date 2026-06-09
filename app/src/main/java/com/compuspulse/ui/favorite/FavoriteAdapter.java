package com.compuspulse.ui.favorite;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.compuspulse.R;
import com.compuspulse.data.local.entity.FavoriteArticleEntity;

import java.util.Objects;

public class FavoriteAdapter extends ListAdapter<FavoriteArticleEntity, FavoriteAdapter.FavoriteViewHolder> {

    private final OnFavoriteActionListener listener;

    public FavoriteAdapter(OnFavoriteActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_article, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteArticleEntity entity = getItem(position);

        holder.tvTitle.setText(entity.getTitle());
        holder.tvSummary.setText(entity.getSummary());
        holder.tvSource.setText(entity.getSource());
        holder.tvTime.setText(entity.getPublishTime());

        if (TextUtils.isEmpty(entity.getImageUrl())) {
            holder.ivCover.setVisibility(View.GONE);
        } else {
            holder.ivCover.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(entity.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.ivCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArticleClick(entity);
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(entity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    private static final DiffUtil.ItemCallback<FavoriteArticleEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<FavoriteArticleEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull FavoriteArticleEntity oldItem,
                                               @NonNull FavoriteArticleEntity newItem) {
                    return oldItem.getArticleId() == newItem.getArticleId()
                            && Objects.equals(oldItem.getUsername(), newItem.getUsername());
                }

                @Override
                public boolean areContentsTheSame(@NonNull FavoriteArticleEntity oldItem,
                                                  @NonNull FavoriteArticleEntity newItem) {
                    return oldItem.getArticleId() == newItem.getArticleId()
                            && Objects.equals(oldItem.getUsername(), newItem.getUsername())
                            && Objects.equals(oldItem.getTitle(), newItem.getTitle())
                            && Objects.equals(oldItem.getSummary(), newItem.getSummary())
                            && Objects.equals(oldItem.getSource(), newItem.getSource())
                            && Objects.equals(oldItem.getSourceUrl(), newItem.getSourceUrl())
                            && Objects.equals(oldItem.getPublishTime(), newItem.getPublishTime())
                            && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                            && oldItem.getCommentCount() == newItem.getCommentCount()
                            && oldItem.getCollectedAt() == newItem.getCollectedAt();
                }
            };

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        ImageView ivDelete;
        TextView tvTitle;
        TextView tvSummary;
        TextView tvSource;
        TextView tvTime;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    public interface OnFavoriteActionListener {
        void onArticleClick(FavoriteArticleEntity entity);
        void onDeleteClick(FavoriteArticleEntity entity);
    }
}
