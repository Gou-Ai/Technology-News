package com.compuspulse.ui.detail

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.compuspulse.R
import com.compuspulse.data.remote.model.Article
import com.compuspulse.data.repository.ArticleLocalRepository
import kotlinx.coroutines.launch

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivFavorite: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    private var article: Article? = null
    private var isFavorite = false
    private var favoriteActionPending = false
    private lateinit var localRepository: ArticleLocalRepository

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        localRepository = ArticleLocalRepository()

        initView()
        getIntentData()
        initWebView()
        bindData()
        initClick()
        saveHistoryIfNeeded()
        checkFavoriteStatus()
    }

    private fun initView() {
        ivBack = findViewById(R.id.iv_back)
        ivFavorite = findViewById(R.id.iv_favorite)
        tvTitle = findViewById(R.id.tv_detail_title)
        webView = findViewById(R.id.wv_article_detail)
        progressBar = findViewById(R.id.pb_web_loading)
    }

    private fun getIntentData() {
        val obj = intent.getSerializableExtra("article")
        if (obj is Article) {
            article = obj
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = false
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: ""
                if (TextUtils.isEmpty(url)) {
                    return true
                }
                return !isSecureWebUrl(url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            }
        }
    }

    private fun bindData() {
        val currentArticle = article
        if (currentArticle == null) {
            tvTitle.text = "文章详情"
            return
        }

        tvTitle.text = currentArticle.getTitle()

        val sourceUrl = currentArticle.getSourceUrl()
        if (isSecureWebUrl(sourceUrl)) {
            webView.loadUrl(sourceUrl)
        } else {
            Toast.makeText(this, "仅支持打开 HTTPS 文章链接", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isSecureWebUrl(url: String?): Boolean {
        return !url.isNullOrBlank() && url.startsWith("https://", ignoreCase = true)
    }

    private fun initClick() {
        ivBack.setOnClickListener { finish() }

        ivFavorite.setOnClickListener {
            val currentArticle = article ?: return@setOnClickListener
            if (favoriteActionPending) {
                return@setOnClickListener
            }

            favoriteActionPending = true
            isFavorite = !isFavorite
            updateFavoriteIcon()

            lifecycleScope.launch {
                try {
                    if (isFavorite) {
                        localRepository.addFavorite(currentArticle)
                            .onFailure { throwable ->
                                isFavorite = false
                                updateFavoriteIcon()
                                Toast.makeText(
                                    this@ArticleDetailActivity,
                                    throwable.message ?: "收藏失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        localRepository.removeFavorite(currentArticle.getServerArticleId())
                            .onFailure { throwable ->
                                isFavorite = true
                                updateFavoriteIcon()
                                Toast.makeText(
                                    this@ArticleDetailActivity,
                                    throwable.message ?: "取消收藏失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } finally {
                    favoriteActionPending = false
                    updateFavoriteIcon()
                }
            }
        }
    }

    private fun saveHistoryIfNeeded() {
        article?.let { currentArticle ->
            lifecycleScope.launch {
                localRepository.saveHistory(currentArticle)
            }
        }
    }

    private fun checkFavoriteStatus() {
        val currentArticle = article ?: return

        localRepository.observeFavoriteState(currentArticle.getId()).observe(this) { result ->
            if (favoriteActionPending) {
                return@observe
            }
            isFavorite = result == true
            updateFavoriteIcon()
        }
    }

    private fun updateFavoriteIcon() {
        ivFavorite.isEnabled = !favoriteActionPending
        ivFavorite.alpha = if (favoriteActionPending) 0.5f else 1f
        if (isFavorite) {
            ivFavorite.setImageResource(R.drawable.ic_favorite_selected)
        } else {
            ivFavorite.setImageResource(R.drawable.ic_favorite_unselected)
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.webChromeClient = null
        webView.webViewClient = WebViewClient()
        webView.destroy()
        super.onDestroy()
    }
}
