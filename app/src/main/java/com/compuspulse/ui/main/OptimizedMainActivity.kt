package com.compuspulse.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.compuspulse.R
import com.compuspulse.data.remote.http.NetworkClient
import com.compuspulse.data.repository.ArticleLocalRepository
import com.compuspulse.data.repository.AuthRepository
import com.compuspulse.ui.auth.LoginActivity
import com.compuspulse.ui.category.CategoryFragment
import com.compuspulse.ui.favorite.FavoriteFragment
import com.compuspulse.ui.history.HistoryFragment
import com.compuspulse.ui.home.HomeFragment
import com.compuspulse.ui.search.SearchActivity
import com.compuspulse.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class OptimizedMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var articleLocalRepository: ArticleLocalRepository
    private var currentTabTag = TAG_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        authRepository = AuthRepository()
        articleLocalRepository = ArticleLocalRepository()
        if (!sessionManager.isLogin() || !NetworkClient.getInstance().hasSessionCookies()) {
            lifecycleScope.launch {
                clearLocalAccountDataAndSession()
                goLogin()
            }
            return
        }

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        if (savedInstanceState != null) {
            currentTabTag = savedInstanceState.getString(STATE_CURRENT_TAB, TAG_HOME) ?: TAG_HOME
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val itemId = item.itemId
            if (itemId == R.id.menu_home) {
                switchFragment(TAG_HOME)
                return@setOnItemSelectedListener true
            }
            if (itemId == R.id.menu_category) {
                switchFragment(TAG_CATEGORY)
                return@setOnItemSelectedListener true
            }
            if (itemId == R.id.menu_favorite) {
                switchFragment(TAG_FAVORITE)
                return@setOnItemSelectedListener true
            }
            if (itemId == R.id.menu_history) {
                switchFragment(TAG_HISTORY)
                return@setOnItemSelectedListener true
            }
            false
        }
        //BottomNavigationView的setSelectedItemId方法设置高亮
        bottomNavigationView.selectedItemId = getMenuItemIdForTag(currentTabTag)
        switchFragment(currentTabTag)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_CURRENT_TAB, currentTabTag)
        super.onSaveInstanceState(outState)
    }

    private fun switchFragment(targetTag: String) {
        var targetFragment = supportFragmentManager.findFragmentByTag(targetTag)
        if (targetFragment == null) {
            targetFragment = createFragment(targetTag)
        }
        //开启事务，隐藏所有其他可见的 Fragment
        val transaction = supportFragmentManager.beginTransaction()
        hideIfPresent(transaction, TAG_HOME, targetTag)
        hideIfPresent(transaction, TAG_CATEGORY, targetTag)
        hideIfPresent(transaction, TAG_FAVORITE, targetTag)
        hideIfPresent(transaction, TAG_HISTORY, targetTag)

        if (targetFragment.isAdded) {
            transaction.show(targetFragment)
        } else {
            transaction.add(R.id.fragment_container, targetFragment, targetTag)
        }
        transaction.commit()

        currentTabTag = targetTag
        title = getTitleForTag(targetTag) + " - " + sessionManager.getCurrentUsername()
    }

    private fun hideIfPresent(transaction: FragmentTransaction, tag: String, targetTag: String) {
        if (tag == targetTag) {
            return
        }
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null && fragment.isAdded && !fragment.isHidden) {
            transaction.hide(fragment)
        }
    }

    private fun createFragment(tag: String): Fragment {
        if (TAG_CATEGORY == tag) {
            return CategoryFragment.newInstance()
        }
        if (TAG_FAVORITE == tag) {
            return FavoriteFragment.newInstance()
        }
        if (TAG_HISTORY == tag) {
            return HistoryFragment.newInstance()
        }
        return HomeFragment.newInstance()
    }

    private fun getTitleForTag(tag: String): String {
        if (TAG_CATEGORY == tag) {
            return "分类"
        }
        if (TAG_FAVORITE == tag) {
            return "收藏"
        }
        if (TAG_HISTORY == tag) {
            return "历史"
        }
        return "首页"
    }

    private fun getMenuItemIdForTag(tag: String): Int {
        if (TAG_CATEGORY == tag) {
            return R.id.menu_category
        }
        if (TAG_FAVORITE == tag) {
            return R.id.menu_favorite
        }
        if (TAG_HISTORY == tag) {
            return R.id.menu_history
        }
        return R.id.menu_home
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.menu_search) {
            startActivity(Intent(this, SearchActivity::class.java))
            return true
        }
        if (itemId == R.id.menu_logout) {
            lifecycleScope.launch {
                authRepository.logout()
                clearLocalAccountDataAndSession()
                goLogin()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun clearLocalAccountDataAndSession() {
        val username = sessionManager.getCurrentUsername()
        articleLocalRepository.clearUserLocalData(username)
        NetworkClient.getInstance().clearCookies()
        sessionManager.logout()
    }

    private fun goLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val STATE_CURRENT_TAB = "state_current_tab"
        private const val TAG_HOME = "tab_home"
        private const val TAG_CATEGORY = "tab_category"
        private const val TAG_FAVORITE = "tab_favorite"
        private const val TAG_HISTORY = "tab_history"
    }
}
