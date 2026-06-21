package com.compuspulse.ui.common

import androidx.recyclerview.widget.DiffUtil
import com.compuspulse.data.remote.model.Article

class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {

    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.getId() == newItem.getId()
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.getTitle() == newItem.getTitle() &&
            oldItem.getSummary() == newItem.getSummary() &&
            oldItem.getSource() == newItem.getSource() &&
            oldItem.getSourceUrl() == newItem.getSourceUrl() &&
            oldItem.getPublishTime() == newItem.getPublishTime() &&
            oldItem.getImageUrl() == newItem.getImageUrl() &&
            oldItem.getCategory() == newItem.getCategory() &&
            oldItem.getCommentCount() == newItem.getCommentCount() &&
            oldItem.isFavorite() == newItem.isFavorite()
    }
}
