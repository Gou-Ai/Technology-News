package com.compuspulse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.Banner;
import com.compuspulse.data.remote.model.PageResponse;
import com.compuspulse.data.repository.HomeFeedRepository;
import com.compuspulse.data.repository.RepositoryCallback;
import com.compuspulse.ui.home.HomeListAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final HomeFeedRepository repository = new HomeFeedRepository();

    private final MutableLiveData<List<Banner>> bannerList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Article>> articleList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> loadMoreStatus =
            new MutableLiveData<>(HomeListAdapter.STATUS_IDLE);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final List<Article> currentArticles = new ArrayList<>();
    private int currentPage = 0;
    private boolean noMoreData = false;
    private boolean isLoadingMore = false;

    public LiveData<List<Banner>> getBannerList() {
        return bannerList;
    }

    public LiveData<List<Article>> getArticleList() {
        return articleList;
    }

    public LiveData<Boolean> getRefreshing() {
        return refreshing;
    }

    public LiveData<Integer> getLoadMoreStatus() {
        return loadMoreStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadHomeData() {
        refreshing.setValue(true);
        loadBanners();
        currentPage = 0;
        noMoreData = false;
        loadArticlePage(true);
    }

    public void refreshArticles() {
        currentPage = 0;
        noMoreData = false;
        refreshing.setValue(true);
        loadBanners();
        loadArticlePage(true);
    }

    public void loadMoreArticles() {
        //防止重复加载，防抖
        if (Boolean.TRUE.equals(refreshing.getValue()) || isLoadingMore) {
            return;
        }
        if (noMoreData) {
            loadMoreStatus.setValue(HomeListAdapter.STATUS_NO_MORE);
            return;
        }
        currentPage++;
        loadMoreStatus.setValue(HomeListAdapter.STATUS_LOADING);
        isLoadingMore = true;
        loadArticlePage(false);
    }

    private void loadBanners() {
        repository.fetchBannerList(new RepositoryCallback<List<Banner>>() {
            @Override
            public void onSuccess(List<Banner> data) {
                bannerList.setValue(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    private void loadArticlePage(boolean clearBeforeAppend) {
        repository.fetchHomeArticles(currentPage, new RepositoryCallback<PageResponse<Article>>() {
            @Override
            public void onSuccess(PageResponse<Article> data) {
                List<Article> pageData = data == null ? new ArrayList<>() : data.getDatas();
                //是否要清除之前缓存的文章
                if (clearBeforeAppend) {
                    currentArticles.clear();
                }
                currentArticles.addAll(pageData);
                articleList.setValue(new ArrayList<>(currentArticles));
                noMoreData = data != null && data.isOver();
                loadMoreStatus.setValue(noMoreData ? HomeListAdapter.STATUS_NO_MORE : HomeListAdapter.STATUS_IDLE);
                refreshing.setValue(false);
                isLoadingMore = false;
            }

            @Override
            public void onError(String message) {
                //加载更多失败则页码回退一页
                if (!clearBeforeAppend && currentPage > 0) {
                    currentPage--;
                }
                errorMessage.setValue(message);
                refreshing.setValue(false);
                isLoadingMore = false;
                loadMoreStatus.setValue(currentArticles.isEmpty() ? HomeListAdapter.STATUS_IDLE : HomeListAdapter.STATUS_LOAD_FAIL);
            }
        });
    }
}
