package com.compuspulse.ui.history

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
import com.compuspulse.data.local.entity.HistoryArticleEntity

class HistoryAdapter(
    private val listener: OnHistoryClickListener?
) : ListAdapter<HistoryArticleEntity, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_article, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
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
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.iv_cover)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvSummary: TextView = itemView.findViewById(R.id.tv_summary)
        val tvSource: TextView = itemView.findViewById(R.id.tv_source)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }

    fun interface OnHistoryClickListener {
        fun onArticleClick(entity: HistoryArticleEntity)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoryArticleEntity>() {
            override fun areItemsTheSame(
                oldItem: HistoryArticleEntity,
                newItem: HistoryArticleEntity
            ): Boolean {
                return oldItem.articleId == newItem.articleId &&
                    oldItem.username == newItem.username
            }

            override fun areContentsTheSame(
                oldItem: HistoryArticleEntity,
                newItem: HistoryArticleEntity
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
                    oldItem.viewedAt == newItem.viewedAt
            }
        }
    }
}
