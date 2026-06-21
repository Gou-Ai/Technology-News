package com.compuspulse.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.compuspulse.R
import com.compuspulse.data.local.entity.FavoriteArticleEntity
import com.compuspulse.data.remote.model.Article
import com.compuspulse.ui.detail.ArticleDetailActivity
import com.compuspulse.viewmodel.FavoriteViewModel

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val favoriteViewModel: FavoriteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initRecyclerView()
        initViewModel()
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_favorite)
        tvEmpty = view.findViewById(R.id.tv_empty_favorite)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        favoriteAdapter = FavoriteAdapter(object : FavoriteAdapter.OnFavoriteActionListener {
            override fun onArticleClick(entity: FavoriteArticleEntity) {
                val article = convertToArticle(entity)
                val intent = Intent(requireContext(), ArticleDetailActivity::class.java)
                intent.putExtra("article", article)
                startActivity(intent)
            }

            override fun onDeleteClick(entity: FavoriteArticleEntity) {
                favoriteViewModel.removeFavorite(entity.articleId)
            }
        })
        recyclerView.adapter = favoriteAdapter
    }

    private fun initViewModel() {
        favoriteViewModel.getFavoriteList().observe(viewLifecycleOwner) { list ->
            favoriteAdapter.submitList(list)
            val isEmpty = list.isNullOrEmpty()
            tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
        favoriteViewModel.getErrorMessage().observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        favoriteViewModel.refreshFavorites()
    }

    private fun convertToArticle(entity: FavoriteArticleEntity): Article {
        val article = Article()
        article.setId(entity.articleId)
        article.setTitle(entity.title)
        article.setSummary(entity.summary)
        article.setSource(entity.source)
        article.setSourceUrl(entity.sourceUrl)
        article.setPublishTime(entity.publishTime)
        article.setImageUrl(entity.imageUrl)
        article.setCommentCount(entity.commentCount)
        article.setFavorite(true)
        return article
    }

    companion object {
        @JvmStatic
        fun newInstance(): FavoriteFragment = FavoriteFragment()
    }
}
