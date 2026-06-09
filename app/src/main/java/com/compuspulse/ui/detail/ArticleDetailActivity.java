package com.compuspulse.ui.detail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.compuspulse.R;
import com.compuspulse.data.remote.model.Article;
import com.compuspulse.data.repository.ArticleLocalRepository;

public class ArticleDetailActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ImageView ivFavorite;
    private TextView tvTitle;
    private WebView webView;
    private ProgressBar progressBar;

    private Article article;
    private boolean isFavorite = false;
    private ArticleLocalRepository localRepository;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        localRepository = new ArticleLocalRepository();

        initView();
        getIntentData();
        initWebView();
        bindData();
        initClick();
        saveHistoryIfNeeded();
        checkFavoriteStatus();
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        ivFavorite = findViewById(R.id.iv_favorite);
        tvTitle = findViewById(R.id.tv_detail_title);
        webView = findViewById(R.id.wv_article_detail);
        progressBar = findViewById(R.id.pb_web_loading);
    }

    private void getIntentData() {
        Object object = getIntent().getSerializableExtra("article");
        if (object instanceof Article) {
            article = (Article) object;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            //是否拦截
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request == null || request.getUrl() == null ? "" : request.getUrl().toString();
                if (TextUtils.isEmpty(url)) {
                    return false;
                }
                return !(url.startsWith("http://") || url.startsWith("https://"));
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void bindData() {
        if (article == null) {
            tvTitle.setText("文章详情");
            return;
        }

        tvTitle.setText(article.getTitle());

        if (!TextUtils.isEmpty(article.getSourceUrl())) {
            webView.loadUrl(article.getSourceUrl());
        }
    }

    private void initClick() {
        ivBack.setOnClickListener(v -> finish());

        ivFavorite.setOnClickListener(v -> {
            if (article == null) {
                return;
            }

            isFavorite = !isFavorite;
            updateFavoriteIcon();

            if (isFavorite) {
                localRepository.addFavorite(article);
            } else {
                localRepository.removeFavorite(article.getId());
            }
        });
    }

    private void saveHistoryIfNeeded() {
        if (article != null) {
            localRepository.saveHistory(article);
        }
    }

    private void checkFavoriteStatus() {
        if (article == null) {
            return;
        }

        localRepository.observeFavoriteState(article.getId()).observe(this, result -> {
            isFavorite = Boolean.TRUE.equals(result);
            updateFavoriteIcon();
        });
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            ivFavorite.setImageResource(R.drawable.ic_favorite_selected);
        } else {
            ivFavorite.setImageResource(R.drawable.ic_favorite_unselected);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }
}
