package com.compuspulse.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.compuspulse.R
import com.compuspulse.ui.detail.ArticleDetailActivity
import com.compuspulse.viewmodel.SearchViewModel
import java.util.Collections

class SearchActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivSearch: ImageView
    private lateinit var etKeyword: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: SearchAdapter
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initView()
        initRecyclerView()
        initViewModel()
        initClick()
    }

    private fun initView() {
        ivBack = findViewById(R.id.iv_back_search)
        ivSearch = findViewById(R.id.iv_do_search)
        etKeyword = findViewById(R.id.et_keyword)
        recyclerView = findViewById(R.id.recycler_view_search)
        tvEmpty = findViewById(R.id.tv_empty_search)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = SearchAdapter { article ->
            val intent = Intent(this, ArticleDetailActivity::class.java)
            intent.putExtra("article", article)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun initViewModel() {
        viewModel.getResultList().observe(this) { list ->
            adapter.submitList(list)
            val isEmpty = list.isNullOrEmpty()
            tvEmpty.text = if (viewModel.getLoading().value == true) "搜索中..." else "暂无搜索结果"
            tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
        viewModel.getLoading().observe(this) { loading ->
            if (loading == true) {
                tvEmpty.text = "搜索中..."
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        }
        viewModel.getErrorMessage().observe(this) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initClick() {
        ivBack.setOnClickListener { finish() }

        ivSearch.setOnClickListener { doSearch() }
        etKeyword.setOnEditorActionListener { _, _, _ ->
            doSearch()
            true
        }
    }

    private fun doSearch() {
        val keyword = etKeyword.text.toString().trim()
        if (!TextUtils.isEmpty(keyword)) {
            viewModel.search(keyword)
        } else {
            adapter.submitList(Collections.emptyList())
            tvEmpty.text = "请输入关键词"
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }
}
