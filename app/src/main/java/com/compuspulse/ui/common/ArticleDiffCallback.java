package com.compuspulse.ui.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.compuspulse.data.remote.model.Article;

import java.util.Objects;

public class ArticleDiffCallback extends DiffUtil.ItemCallback<Article> {

    @Override
    public boolean areItemsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
        return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                && Objects.equals(oldItem.getSummary(), newItem.getSummary())
                && Objects.equals(oldItem.getSource(), newItem.getSource())
                && Objects.equals(oldItem.getSourceUrl(), newItem.getSourceUrl())
                && Objects.equals(oldItem.getPublishTime(), newItem.getPublishTime())
                && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                && Objects.equals(oldItem.getCategory(), newItem.getCategory())
                && oldItem.getCommentCount() == newItem.getCommentCount()
                && oldItem.isFavorite() == newItem.isFavorite();
    }
}
