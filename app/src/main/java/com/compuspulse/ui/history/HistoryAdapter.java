package com.compuspulse.ui.history;

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
import com.compuspulse.data.local.entity.HistoryArticleEntity;

import java.util.Objects;

public class HistoryAdapter extends ListAdapter<HistoryArticleEntity, HistoryAdapter.HistoryViewHolder> {

    private final OnHistoryClickListener listener;

    public HistoryAdapter(OnHistoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_article, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryArticleEntity entity = getItem(position);

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
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    private static final DiffUtil.ItemCallback<HistoryArticleEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<HistoryArticleEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull HistoryArticleEntity oldItem,
                                               @NonNull HistoryArticleEntity newItem) {
                    return oldItem.getArticleId() == newItem.getArticleId()
                            && Objects.equals(oldItem.getUsername(), newItem.getUsername());
                }

                @Override
                public boolean areContentsTheSame(@NonNull HistoryArticleEntity oldItem,
                                                  @NonNull HistoryArticleEntity newItem) {
                    return oldItem.getArticleId() == newItem.getArticleId()
                            && Objects.equals(oldItem.getUsername(), newItem.getUsername())
                            && Objects.equals(oldItem.getTitle(), newItem.getTitle())
                            && Objects.equals(oldItem.getSummary(), newItem.getSummary())
                            && Objects.equals(oldItem.getSource(), newItem.getSource())
                            && Objects.equals(oldItem.getSourceUrl(), newItem.getSourceUrl())
                            && Objects.equals(oldItem.getPublishTime(), newItem.getPublishTime())
                            && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                            && oldItem.getCommentCount() == newItem.getCommentCount()
                            && oldItem.getViewedAt() == newItem.getViewedAt();
                }
            };

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvSummary;
        TextView tvSource;
        TextView tvTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    public interface OnHistoryClickListener {
        void onArticleClick(HistoryArticleEntity entity);
    }
}
