package com.compuspulse.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.compuspulse.R
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.repository.ArticleLocalRepository
import com.compuspulse.ui.detail.ArticleDetailActivity
import com.compuspulse.viewmodel.HomeViewModel
import java.util.Collections
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var homeAdapter: HomeListAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var articleLocalRepository: ArticleLocalRepository
    private var lastVisibleItem = 0
    private var currentLoadMoreStatus = HomeListAdapter.STATUS_IDLE
    private val pendingFavoriteIds = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //拿到控件
        initView(view)
        //绑定列表
        initRecyclerView()
        //绑定刷新
        initRefresh()
        //订阅数据
        initViewModel()
    }

    private fun initView(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        recyclerView = view.findViewById(R.id.recycler_view)
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        homeAdapter = HomeListAdapter(
            context = requireContext(),
            onArticleClick = ::openArticle,
            onArticleFavoriteClick = ::collectArticle
        )
        articleLocalRepository = ArticleLocalRepository()

        recyclerView.adapter = homeAdapter
        attachSwipeToFavorite()
        setupBackPressToCloseSwipe()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if ((dx != 0 || dy != 0) && homeAdapter.hasOpenedSwipe()) {
                    homeAdapter.closeSwipe()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                    lastVisibleItem == homeAdapter.itemCount - 1
                ) {
                    homeViewModel.loadMoreArticles()
                }
            }
        })
    }

    private fun openArticle(article: Article) {
        val intent = Intent(requireContext(), ArticleDetailActivity::class.java)
        intent.putExtra("article", article)
        startActivity(intent)
    }

    private fun collectArticle(article: Article) {
        val articleId = article.getServerArticleId()
        if (!pendingFavoriteIds.add(articleId)) {
            return
        }
        if (article.isFavorite()) {
            pendingFavoriteIds.remove(articleId)
            Toast.makeText(requireContext(), "已经添加到了喜爱中", Toast.LENGTH_SHORT).show()
            return
        }
        article.setFavorite(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                articleLocalRepository.addFavorite(article)
                    .onSuccess {
                        Toast.makeText(requireContext(), "添加到喜爱中", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { throwable ->
                        article.setFavorite(false)
                        Toast.makeText(
                            requireContext(),
                            throwable.message ?: "添加到喜爱失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } finally {
                pendingFavoriteIds.remove(articleId)
            }
        }
    }

    private fun attachSwipeToFavorite() {
        val swipeCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                //不支持拖动移动
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                //若是RecyclerView中的item，只允许左滑
                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val position = viewHolder.bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION || !homeAdapter.isArticlePosition(position)) {
                        return 0
                    }
                    return if (homeAdapter.isSwipeOpen(position)) {
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    } else {
                        ItemTouchHelper.LEFT
                    }
                }

                //滑动15%触发
                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.15f

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return
                    }

                    if (direction == ItemTouchHelper.LEFT) {
                        homeAdapter.openSwipeAt(position)
                    } else {
                        homeAdapter.closeSwipeAt(position)
                    }
                }
            }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun setupBackPressToCloseSwipe() {
        //返回键拦截，返回键优先关闭左滑菜单栏
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (homeAdapter.hasOpenedSwipe()) {
                        homeAdapter.closeSwipe()
                        return
                    }
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        )
    }

    private fun initRefresh() {
        swipeRefreshLayout.setOnRefreshListener { homeViewModel.refreshArticles() }
    }

    private fun initViewModel() {
        //LiveData跟View生命周期绑定
        homeViewModel.getBannerList().observe(viewLifecycleOwner) { renderHomeList() }

        homeViewModel.getArticleList().observe(viewLifecycleOwner) { renderHomeList() }

        homeViewModel.getRefreshing().observe(viewLifecycleOwner) { refreshing ->
            swipeRefreshLayout.isRefreshing = refreshing == true
        }

        homeViewModel.getLoadMoreStatus().observe(viewLifecycleOwner) { status ->
            if (status != null) {
                currentLoadMoreStatus = status
                renderHomeList()
            }
        }

        homeViewModel.getErrorMessage().observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        homeViewModel.loadHomeData()
    }

    private fun renderHomeList() {
        if (!::homeAdapter.isInitialized) {
            return
        }
        homeAdapter.submitData(
            homeViewModel.getBannerList().value ?: Collections.emptyList(),
            homeViewModel.getArticleList().value ?: Collections.emptyList(),
            currentLoadMoreStatus
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment = HomeFragment()
    }
}
