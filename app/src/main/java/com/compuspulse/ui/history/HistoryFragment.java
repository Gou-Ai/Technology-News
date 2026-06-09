package com.compuspulse.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.compuspulse.R;
import com.compuspulse.data.local.entity.HistoryArticleEntity;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.ui.detail.ArticleDetailActivity;
import com.compuspulse.viewmodel.HistoryViewModel;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvClear;
    private TextView tvEmpty;
    private HistoryAdapter historyAdapter;
    private HistoryViewModel historyViewModel;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initRecyclerView();
        initViewModel();
        initClick();
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_history);
        tvClear = view.findViewById(R.id.tv_clear_history);
        tvEmpty = view.findViewById(R.id.tv_empty_history);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        historyAdapter = new HistoryAdapter(entity -> {
                    Article article = convertToArticle(entity);
                    Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
                    intent.putExtra("article", article);
                    startActivity(intent);
                });
        recyclerView.setAdapter(historyAdapter);
    }

    private void initViewModel() {
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        historyViewModel.getHistoryList().observe(getViewLifecycleOwner(), list -> {
            historyAdapter.submitList(list);
            boolean isEmpty = list == null || list.isEmpty();
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void initClick() {
        tvClear.setOnClickListener(v -> historyViewModel.clearHistory());
    }

    private Article convertToArticle(HistoryArticleEntity entity) {
        Article article = new Article();
        article.setId(entity.getArticleId());
        article.setTitle(entity.getTitle());
        article.setSummary(entity.getSummary());
        article.setSource(entity.getSource());
        article.setSourceUrl(entity.getSourceUrl());
        article.setPublishTime(entity.getPublishTime());
        article.setImageUrl(entity.getImageUrl());
        article.setCommentCount(entity.getCommentCount());
        article.setFavorite(false);
        return article;
    }
}
