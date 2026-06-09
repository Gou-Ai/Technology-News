package com.compuspulse.ui.home;

import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.remote.model.Banner;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeListItem {

    public static final int TYPE_BANNER = 0;
    public static final int TYPE_ARTICLE = 1;
    public static final int TYPE_FOOTER = 2;

    private final int type;
    private final List<Banner> banners;
    private final Article article;
    private final int footerStatus;

    private HomeListItem(int type, List<Banner> banners, Article article, int footerStatus) {
        this.type = type;
        this.banners = banners == null ? Collections.emptyList() : banners;
        this.article = article;
        this.footerStatus = footerStatus;
    }

    public static HomeListItem banner(List<Banner> banners) {
        return new HomeListItem(TYPE_BANNER, banners, null, HomeListAdapter.STATUS_IDLE);
    }

    public static HomeListItem article(Article article) {
        return new HomeListItem(TYPE_ARTICLE, null, article, HomeListAdapter.STATUS_IDLE);
    }

    public static HomeListItem footer(int footerStatus) {
        return new HomeListItem(TYPE_FOOTER, null, null, footerStatus);
    }

    public int getType() {
        return type;
    }

    public List<Banner> getBanners() {
        return banners;
    }

    public Article getArticle() {
        return article;
    }

    public int getFooterStatus() {
        return footerStatus;
    }

    public long getStableId() {
        if (type == TYPE_BANNER) {
            return Long.MIN_VALUE;
        }
        if (type == TYPE_FOOTER) {
            return Long.MAX_VALUE;
        }
        return article == null ? RecyclerIds.EMPTY_ARTICLE_ID : article.getId();
    }

    public boolean isContentSame(HomeListItem other) {
        if (other == null || type != other.type) {
            return false;
        }
        if (type == TYPE_BANNER) {
            return sameBannerList(banners, other.banners);
        }
        if (type == TYPE_FOOTER) {
            return footerStatus == other.footerStatus;
        }
        if (article == null || other.article == null) {
            return article == other.article;
        }
        return article.getId() == other.article.getId()
                && Objects.equals(article.getTitle(), other.article.getTitle())
                && Objects.equals(article.getSummary(), other.article.getSummary())
                && Objects.equals(article.getSource(), other.article.getSource())
                && Objects.equals(article.getSourceUrl(), other.article.getSourceUrl())
                && Objects.equals(article.getPublishTime(), other.article.getPublishTime())
                && Objects.equals(article.getImageUrl(), other.article.getImageUrl())
                && Objects.equals(article.getCategory(), other.article.getCategory())
                && article.getCommentCount() == other.article.getCommentCount()
                && article.isFavorite() == other.article.isFavorite();
    }

    private boolean sameBannerList(List<Banner> oldList, List<Banner> newList) {
        if (oldList == newList) {
            return true;
        }
        if (oldList == null || newList == null || oldList.size() != newList.size()) {
            return false;
        }
        for (int index = 0; index < oldList.size(); index++) {
            Banner oldBanner = oldList.get(index);
            Banner newBanner = newList.get(index);
            if (oldBanner == null || newBanner == null) {
                if (oldBanner != newBanner) {
                    return false;
                }
                continue;
            }
            if (oldBanner.getId() != newBanner.getId()
                    || !Objects.equals(oldBanner.getTitle(), newBanner.getTitle())
                    || !Objects.equals(oldBanner.getImageUrl(), newBanner.getImageUrl())
                    || !Objects.equals(oldBanner.getTargetUrl(), newBanner.getTargetUrl())) {
                return false;
            }
        }
        return true;
    }

    private static final class RecyclerIds {
        private static final long EMPTY_ARTICLE_ID = -2L;

        private RecyclerIds() {
        }
    }
}
