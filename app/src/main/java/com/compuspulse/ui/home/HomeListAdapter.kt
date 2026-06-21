package com.compuspulse.ui.home

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.compuspulse.R
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.remote.model.Banner

class HomeListAdapter(
    private val context: Context,
    private val onArticleClick: (Article) -> Unit,
    private val onArticleFavoriteClick: (Article) -> Unit
) : ListAdapter<HomeListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val swipeActionWidthPx: Int = dpToPx(context, 56)
    private var openedItemId = NO_OPENED_ITEM_ID

    init {
        setHasStableIds(true)
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    fun submitData(banners: List<Banner>?, articles: List<Article>?, loadMoreStatus: Int) {
        val items = ArrayList<HomeListItem>()
        //将外部的banner、article、loadMoreStatus转换为HomeListItem，放入item列表中
        items.add(HomeListItem.banner(if (banners == null) emptyList() else ArrayList(banners)))

        articles?.forEach { article ->
            items.add(HomeListItem.article(article))
        }

        items.add(HomeListItem.footer(loadMoreStatus))
        submitList(items)
    }

    override fun getItemId(position: Int): Long = getItem(position).stableId

    override fun getItemViewType(position: Int): Int = getItem(position).type

    fun isArticlePosition(position: Int): Boolean {
        if (position < 0 || position >= itemCount) {
            return false
        }
        return getItem(position).type == HomeListItem.TYPE_ARTICLE
    }

    fun getArticleAt(position: Int): Article? {
        if (!isArticlePosition(position)) {
            return null
        }
        return getItem(position).article
    }

    fun isSwipeOpen(position: Int): Boolean {
        if (!isArticlePosition(position)) {
            return false
        }
        return getItem(position).stableId == openedItemId
    }

    fun hasOpenedSwipe(): Boolean = openedItemId != NO_OPENED_ITEM_ID

    fun openSwipeAt(position: Int) {
        if (!isArticlePosition(position)) {
            notifyItemChanged(position)
            return
        }

        val targetId = getItem(position).stableId
        val previousPosition = findPositionByStableId(openedItemId)
        openedItemId = targetId

        if (previousPosition != RecyclerView.NO_POSITION && previousPosition != position) {
            notifyItemChanged(previousPosition)
        }
        notifyItemChanged(position)
    }

    fun closeSwipeAt(position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }
        if (isSwipeOpen(position)) {
            openedItemId = NO_OPENED_ITEM_ID
        }
        notifyItemChanged(position)
    }

    fun closeSwipe() {
        val previousPosition = findPositionByStableId(openedItemId)
        openedItemId = NO_OPENED_ITEM_ID
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        if (viewType == HomeListItem.TYPE_BANNER) {
            return BannerViewHolder(inflater.inflate(R.layout.item_home_banner_container, parent, false))
        }
        if (viewType == HomeListItem.TYPE_FOOTER) {
            return FooterViewHolder(inflater.inflate(R.layout.item_home_footer, parent, false))
        }
        return ArticleViewHolder(inflater.inflate(R.layout.item_article, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is BannerViewHolder) {
            bindBanner(holder, item.banners)
        } else if (holder is FooterViewHolder) {
            bindFooter(holder, item.footerStatus)
        } else if (holder is ArticleViewHolder) {
            bindArticle(holder, item.article, position)
        }
    }

    private fun bindBanner(holder: BannerViewHolder, banners: List<Banner>) {
        val bannerAdapter = BannerAdapter(context, banners) { banner ->
            val article = Article()
            article.setId(banner.getId())
            article.setTitle(banner.getTitle())
            article.setSummary("来自首页轮播图的推荐内容")
            article.setSource("WanAndroid Banner")
            article.setSourceUrl(banner.getTargetUrl())
            article.setPublishTime("推荐内容")
            onArticleClick(article)
        }
        holder.viewPager.adapter = bannerAdapter
    }

    private fun bindArticle(holder: ArticleViewHolder, article: Article?, position: Int) {
        if (article == null) {
            return
        }
        //通过平移文章的容器和按钮的容器实现滑动打开侧边栏
        val swipeOpen = isSwipeOpen(position)
        holder.contentContainer.translationX = if (swipeOpen) -swipeActionWidthPx.toFloat() else 0f
        holder.favoriteAction.setImageResource(
            if (article.isFavorite()) R.drawable.ic_favorite_selected else R.drawable.ic_favorite_unselected
        )
        holder.favoriteActionContainer.setOnClickListener {
            onArticleFavoriteClick(article)
            closeSwipe()
        }

        holder.tvTitle.text = article.getTitle()
        holder.tvSummary.text = article.getSummary()
        holder.tvSource.text = article.getSource()
        holder.tvTime.text = article.getPublishTime()
        holder.tvCommentCount.text =
            if (article.getCommentCount() > 0) "热度 " + article.getCommentCount() else article.getCategory()

        if (TextUtils.isEmpty(article.getImageUrl())) {
            holder.ivCover.visibility = View.GONE
        } else {
            holder.ivCover.visibility = View.VISIBLE
            Glide.with(context)
                .load(article.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.ivCover)
        }

        holder.itemView.setOnClickListener {
            onArticleClick(article)
        }
    }

    private fun bindFooter(holder: FooterViewHolder, loadMoreStatus: Int) {
        if (loadMoreStatus == STATUS_LOADING) {
            holder.progressBar.visibility = View.VISIBLE
            holder.tvStatus.text = "正在加载..."
        } else if (loadMoreStatus == STATUS_NO_MORE) {
            holder.progressBar.visibility = View.GONE
            holder.tvStatus.text = "没有更多内容了"
        } else if (loadMoreStatus == STATUS_LOAD_FAIL) {
            holder.progressBar.visibility = View.GONE
            holder.tvStatus.text = "加载失败，继续上拉重试"
        } else {
            holder.progressBar.visibility = View.GONE
            holder.tvStatus.text = "上拉加载更多"
        }
    }

    class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewPager: ViewPager = itemView.findViewById(R.id.homepage_ViewPager)
    }

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contentContainer: LinearLayout = itemView.findViewById(R.id.article_content_container)
        val favoriteActionContainer: FrameLayout = itemView.findViewById(R.id.swipe_action_favorite_container)
        val favoriteAction: ImageView = itemView.findViewById(R.id.iv_swipe_favorite)
        val ivCover: ImageView = itemView.findViewById(R.id.iv_cover)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvSummary: TextView = itemView.findViewById(R.id.tv_summary)
        val tvSource: TextView = itemView.findViewById(R.id.tv_source)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar: ProgressBar = itemView.findViewById(R.id.pb_loading)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_footer_status)
    }

    private fun findPositionByStableId(stableId: Long): Int {
        if (stableId == NO_OPENED_ITEM_ID) {
            return RecyclerView.NO_POSITION
        }
        for (i in 0 until itemCount) {
            if (getItem(i).stableId == stableId) {
                return i
            }
        }
        return RecyclerView.NO_POSITION
    }

    companion object {
        const val STATUS_IDLE = 0
        const val STATUS_LOADING = 1
        const val STATUS_NO_MORE = 2
        const val STATUS_LOAD_FAIL = 3
        private const val NO_OPENED_ITEM_ID = -3L

        //dp转换为像素值确保各设备一致
        private fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density + 0.5f).toInt()
        }

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HomeListItem>() {
            override fun areItemsTheSame(oldItem: HomeListItem, newItem: HomeListItem): Boolean {
                return oldItem.type == newItem.type &&
                    oldItem.stableId == newItem.stableId
            }

            override fun areContentsTheSame(oldItem: HomeListItem, newItem: HomeListItem): Boolean {
                return oldItem.isContentSame(newItem)
            }
        }
    }
}
