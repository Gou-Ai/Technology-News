package com.compuspulse.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.compuspulse.R;
import com.compuspulse.ui.detail.ArticleDetailActivity;
import com.compuspulse.viewmodel.SearchViewModel;

import java.util.Collections;

public class SearchActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ImageView ivSearch;
    private EditText etKeyword;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private SearchAdapter adapter;
    private SearchViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        initRecyclerView();
        initViewModel();
        initClick();
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back_search);
        ivSearch = findViewById(R.id.iv_do_search);
        etKeyword = findViewById(R.id.et_keyword);
        recyclerView = findViewById(R.id.recycler_view_search);
        tvEmpty = findViewById(R.id.tv_empty_search);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new SearchAdapter(article -> {
            Intent intent = new Intent(this, ArticleDetailActivity.class);
            intent.putExtra("article", article);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        viewModel.getResultList().observe(this, list -> {
            adapter.submitList(list);
            boolean isEmpty = list == null || list.isEmpty();
            tvEmpty.setText(Boolean.TRUE.equals(viewModel.getLoading().getValue()) ? "搜索中..." : "暂无搜索结果");
            tvEmpty.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
            recyclerView.setVisibility(isEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
        });
        viewModel.getLoading().observe(this, loading -> {
            if (Boolean.TRUE.equals(loading)) {
                tvEmpty.setText("搜索中...");
                tvEmpty.setVisibility(android.view.View.VISIBLE);
                recyclerView.setVisibility(android.view.View.GONE);
            }
        });
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initClick() {
        ivBack.setOnClickListener(v -> finish());

        ivSearch.setOnClickListener(v -> doSearch());
        etKeyword.setOnEditorActionListener((v, actionId, event) -> {
            doSearch();
            return true;
        });
    }

    private void doSearch() {
        String keyword = etKeyword.getText().toString().trim();
        if (!TextUtils.isEmpty(keyword)) {
            viewModel.search(keyword);
        } else {
            adapter.submitList(Collections.emptyList());
            tvEmpty.setText("请输入关键词");
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            recyclerView.setVisibility(android.view.View.GONE);
        }
    }
}
