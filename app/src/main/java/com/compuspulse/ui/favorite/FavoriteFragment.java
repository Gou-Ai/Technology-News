package com.compuspulse.ui.favorite;

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
import com.compuspulse.data.local.entity.FavoriteArticleEntity;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.ui.detail.ArticleDetailActivity;
import com.compuspulse.viewmodel.FavoriteViewModel;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FavoriteAdapter favoriteAdapter;
    private FavoriteViewModel favoriteViewModel;

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initRecyclerView();
        initViewModel();
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_favorite);
        tvEmpty = view.findViewById(R.id.tv_empty_favorite);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        favoriteAdapter = new FavoriteAdapter(
                new FavoriteAdapter.OnFavoriteActionListener() {
                    @Override
                    public void onArticleClick(FavoriteArticleEntity entity) {
                        Article article = convertToArticle(entity);
                        Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
                        intent.putExtra("article", article);
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteClick(FavoriteArticleEntity entity) {
                        favoriteViewModel.removeFavorite(entity.getArticleId());
                    }
                }
        );
        recyclerView.setAdapter(favoriteAdapter);
    }

    private void initViewModel() {
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
        favoriteViewModel.getFavoriteList().observe(getViewLifecycleOwner(), list -> {
            favoriteAdapter.submitList(list);
            boolean isEmpty = list == null || list.isEmpty();
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private Article convertToArticle(FavoriteArticleEntity entity) {
        Article article = new Article();
        article.setId(entity.getArticleId());
        article.setTitle(entity.getTitle());
        article.setSummary(entity.getSummary());
        article.setSource(entity.getSource());
        article.setSourceUrl(entity.getSourceUrl());
        article.setPublishTime(entity.getPublishTime());
        article.setImageUrl(entity.getImageUrl());
        article.setCommentCount(entity.getCommentCount());
        article.setFavorite(true);
        return article;
    }
}
