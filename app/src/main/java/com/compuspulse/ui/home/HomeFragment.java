package com.compuspulse.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.compuspulse.R;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.repository.ArticleLocalRepository;
import com.compuspulse.ui.detail.ArticleDetailActivity;
import com.compuspulse.viewmodel.HomeViewModel;

import java.util.Collections;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private HomeListAdapter homeAdapter;
    private LinearLayoutManager layoutManager;
    private HomeViewModel homeViewModel;
    private ArticleLocalRepository articleLocalRepository;
    private int lastVisibleItem;
    private int currentLoadMoreStatus = HomeListAdapter.STATUS_IDLE;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //拿到控件
        initView(view);
        //绑定列表
        initRecyclerView();
        //绑定刷新
        initRefresh();
        //订阅数据
        initViewModel();
    }

    private void initView(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        homeAdapter = new HomeListAdapter(
                requireContext(),
                article -> {
                    Intent intent = new Intent(requireContext(), ArticleDetailActivity.class);
                    intent.putExtra("article", article);
                    startActivity(intent);
                },
                article -> {
                    if (article == null) {
                        return;
                    }
                    if (article.isFavorite()) {
                        Toast.makeText(requireContext(), "已经添加到了喜爱中", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    article.setFavorite(true);
                    articleLocalRepository.addFavorite(article);
                    Toast.makeText(requireContext(), "添加到喜爱中", Toast.LENGTH_SHORT).show();
                }
        );
        articleLocalRepository = new ArticleLocalRepository();

        recyclerView.setAdapter(homeAdapter);
        attachSwipeToFavorite();
        setupBackPressToCloseSwipe();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if ((dx != 0 || dy != 0) && homeAdapter.hasOpenedSwipe()) {
                    homeAdapter.closeSwipe();
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem == homeAdapter.getItemCount() - 1) {
                    homeViewModel.loadMoreArticles();
                }
            }
        });
    }

    private void attachSwipeToFavorite() {
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    //不支持拖动移动
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    //若是RecyclerView中的item，只允许左滑
                    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder) {
                        int position = viewHolder.getBindingAdapterPosition();
                        if (position == RecyclerView.NO_POSITION || !homeAdapter.isArticlePosition(position)) {
                            return 0;
                        }
                        return homeAdapter.isSwipeOpen(position)
                                ? ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
                                : ItemTouchHelper.LEFT;
                    }

                    @Override
                    //滑动15%触发
                    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                        return 0.15f;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getBindingAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) {
                            return;
                        }

                        if (direction == ItemTouchHelper.LEFT) {
                            homeAdapter.openSwipeAt(position);
                        } else {
                            homeAdapter.closeSwipeAt(position);
                        }
                    }
                };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void setupBackPressToCloseSwipe() {
        //返回键拦截，返回键优先关闭左滑菜单栏
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (homeAdapter != null && homeAdapter.hasOpenedSwipe()) {
                            homeAdapter.closeSwipe();
                            return;
                        }
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        setEnabled(true);
                    }
                }
        );
    }

    private void initRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> homeViewModel.refreshArticles());
    }

    private void initViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getBannerList().observe(getViewLifecycleOwner(), banners -> renderHomeList());

        homeViewModel.getArticleList().observe(getViewLifecycleOwner(), articles -> renderHomeList());

        homeViewModel.getRefreshing().observe(getViewLifecycleOwner(), refreshing ->
                swipeRefreshLayout.setRefreshing(Boolean.TRUE.equals(refreshing)));

        homeViewModel.getLoadMoreStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                currentLoadMoreStatus = status;
                renderHomeList();
            }
        });

        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        homeViewModel.loadHomeData();
    }

    private void renderHomeList() {
        if (homeAdapter == null || homeViewModel == null) {
            return;
        }
        homeAdapter.submitData(
                homeViewModel.getBannerList().getValue() == null
                        ? Collections.emptyList()
                        : homeViewModel.getBannerList().getValue(),
                homeViewModel.getArticleList().getValue() == null
                        ? Collections.emptyList()
                        : homeViewModel.getArticleList().getValue(),
                currentLoadMoreStatus
        );
    }
}
