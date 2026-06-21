package com.compuspulse.ui.category

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.compuspulse.R
import com.compuspulse.data.remote.model.Article
import com.compuspulse.ui.common.ArticleDiffCallback

class CategoryArticleAdapter(
    private val listener: OnCategoryArticleClickListener?
) : ListAdapter<Article, CategoryArticleAdapter.ViewHolder>(ArticleDiffCallback()) {

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = getItem(position)

        holder.tvTitle.text = article.getTitle()
        holder.tvSummary.text = article.getSummary()
        holder.tvSource.text = article.getSource() + " · " + article.getCategory()
        holder.tvTime.text = article.getPublishTime()

        if (TextUtils.isEmpty(article.getImageUrl())) {
            holder.ivCover.visibility = View.GONE
        } else {
            holder.ivCover.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(article.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.ivCover)
        }

        holder.itemView.setOnClickListener {
            listener?.onArticleClick(article)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.iv_cover)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvSummary: TextView = itemView.findViewById(R.id.tv_summary)
        val tvSource: TextView = itemView.findViewById(R.id.tv_source)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }

    fun interface OnCategoryArticleClickListener {
        fun onArticleClick(article: Article)
    }
}
