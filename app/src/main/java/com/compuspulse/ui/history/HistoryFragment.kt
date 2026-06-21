package com.compuspulse.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.compuspulse.R
import com.compuspulse.data.local.entity.HistoryArticleEntity
import com.compuspulse.data.remote.model.Article
import com.compuspulse.ui.detail.ArticleDetailActivity
import com.compuspulse.viewmodel.HistoryViewModel

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvClear: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initRecyclerView()
        initViewModel()
        initClick()
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_history)
        tvClear = view.findViewById(R.id.tv_clear_history)
        tvEmpty = view.findViewById(R.id.tv_empty_history)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        historyAdapter = HistoryAdapter { entity ->
            val article = convertToArticle(entity)
            val intent = Intent(requireContext(), ArticleDetailActivity::class.java)
            intent.putExtra("article", article)
            startActivity(intent)
        }
        recyclerView.adapter = historyAdapter
    }

    private fun initViewModel() {
        historyViewModel.getHistoryList().observe(viewLifecycleOwner) { list ->
            historyAdapter.submitList(list)
            val isEmpty = list.isNullOrEmpty()
            tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun initClick() {
        tvClear.setOnClickListener { historyViewModel.clearHistory() }
    }

    private fun convertToArticle(entity: HistoryArticleEntity): Article {
        val article = Article()
        article.setId(entity.articleId)
        article.setTitle(entity.title)
        article.setSummary(entity.summary)
        article.setSource(entity.source)
        article.setSourceUrl(entity.sourceUrl)
        article.setPublishTime(entity.publishTime)
        article.setImageUrl(entity.imageUrl)
        article.setCommentCount(entity.commentCount)
        article.setFavorite(false)
        return article
    }

    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment = HistoryFragment()
    }
}
