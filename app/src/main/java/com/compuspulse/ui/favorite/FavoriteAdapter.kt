package com.compuspulse.ui.favorite

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.compuspulse.R
import com.compuspulse.data.local.entity.FavoriteArticleEntity

class FavoriteAdapter(
    private val listener: OnFavoriteActionListener?
) : ListAdapter<FavoriteArticleEntity, FavoriteAdapter.FavoriteViewHolder>(DIFF_CALLBACK) {

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_article, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val entity = getItem(position)

        holder.tvTitle.text = entity.title
        holder.tvSummary.text = entity.summary
        holder.tvSource.text = entity.source
        holder.tvTime.text = entity.publishTime

        if (TextUtils.isEmpty(entity.imageUrl)) {
            holder.ivCover.visibility = View.GONE
        } else {
            holder.ivCover.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(entity.imageUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.ivCover)
        }

        holder.itemView.setOnClickListener {
            listener?.onArticleClick(entity)
        }

        holder.ivDelete.setOnClickListener {
            listener?.onDeleteClick(entity)
        }
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.iv_cover)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvSummary: TextView = itemView.findViewById(R.id.tv_summary)
        val tvSource: TextView = itemView.findViewById(R.id.tv_source)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }

    interface OnFavoriteActionListener {
        fun onArticleClick(entity: FavoriteArticleEntity)
        fun onDeleteClick(entity: FavoriteArticleEntity)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FavoriteArticleEntity>() {
            override fun areItemsTheSame(
                oldItem: FavoriteArticleEntity,
                newItem: FavoriteArticleEntity
            ): Boolean {
                return oldItem.articleId == newItem.articleId &&
                    oldItem.username == newItem.username
            }

            override fun areContentsTheSame(
                oldItem: FavoriteArticleEntity,
                newItem: FavoriteArticleEntity
            ): Boolean {
                return oldItem.articleId == newItem.articleId &&
                    oldItem.username == newItem.username &&
                    oldItem.title == newItem.title &&
                    oldItem.summary == newItem.summary &&
                    oldItem.source == newItem.source &&
                    oldItem.sourceUrl == newItem.sourceUrl &&
                    oldItem.publishTime == newItem.publishTime &&
                    oldItem.imageUrl == newItem.imageUrl &&
                    oldItem.commentCount == newItem.commentCount &&
                    oldItem.collectedAt == newItem.collectedAt
            }
        }
    }
}
