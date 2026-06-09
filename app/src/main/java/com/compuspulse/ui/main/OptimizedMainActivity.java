package com.compuspulse.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.compuspulse.R;
import com.compuspulse.ui.auth.LoginActivity;
import com.compuspulse.ui.category.CategoryFragment;
import com.compuspulse.ui.favorite.FavoriteFragment;
import com.compuspulse.ui.history.HistoryFragment;
import com.compuspulse.ui.home.HomeFragment;
import com.compuspulse.ui.search.SearchActivity;
import com.compuspulse.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OptimizedMainActivity extends AppCompatActivity {

    private static final String STATE_CURRENT_TAB = "state_current_tab";
    private static final String TAG_HOME = "tab_home";
    private static final String TAG_CATEGORY = "tab_category";
    private static final String TAG_FAVORITE = "tab_favorite";
    private static final String TAG_HISTORY = "tab_history";

    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;
    private String currentTabTag = TAG_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLogin()) {
            goLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (savedInstanceState != null) {
            currentTabTag = savedInstanceState.getString(STATE_CURRENT_TAB, TAG_HOME);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_home) {
                switchFragment(TAG_HOME);
                return true;
            }
            if (itemId == R.id.menu_category) {
                switchFragment(TAG_CATEGORY);
                return true;
            }
            if (itemId == R.id.menu_favorite) {
                switchFragment(TAG_FAVORITE);
                return true;
            }
            if (itemId == R.id.menu_history) {
                switchFragment(TAG_HISTORY);
                return true;
            }
            return false;
        });
        //BottomNavigationView的setSelectedItemId方法设置高亮
        bottomNavigationView.setSelectedItemId(getMenuItemIdForTag(currentTabTag));
        switchFragment(currentTabTag);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_CURRENT_TAB, currentTabTag);
        super.onSaveInstanceState(outState);
    }

    private void switchFragment(String targetTag) {
        Fragment targetFragment = getSupportFragmentManager().findFragmentByTag(targetTag);
        if (targetFragment == null) {
            targetFragment = createFragment(targetTag);
        }
        //开启事务，隐藏所有其他可见的 Fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideIfPresent(transaction, TAG_HOME, targetTag);
        hideIfPresent(transaction, TAG_CATEGORY, targetTag);
        hideIfPresent(transaction, TAG_FAVORITE, targetTag);
        hideIfPresent(transaction, TAG_HISTORY, targetTag);

        if (targetFragment.isAdded()) {
            transaction.show(targetFragment);
        } else {
            transaction.add(R.id.fragment_container, targetFragment, targetTag);
        }
        transaction.commit();

        currentTabTag = targetTag;
        setTitle(getTitleForTag(targetTag) + " - " + sessionManager.getCurrentUsername());
    }

    private void hideIfPresent(FragmentTransaction transaction, String tag, String targetTag) {
        if (tag.equals(targetTag)) {
            return;
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && fragment.isAdded() && !fragment.isHidden()) {
            transaction.hide(fragment);
        }
    }

    private Fragment createFragment(String tag) {
        if (TAG_CATEGORY.equals(tag)) {
            return CategoryFragment.newInstance();
        }
        if (TAG_FAVORITE.equals(tag)) {
            return FavoriteFragment.newInstance();
        }
        if (TAG_HISTORY.equals(tag)) {
            return HistoryFragment.newInstance();
        }
        return HomeFragment.newInstance();
    }

    private String getTitleForTag(String tag) {
        if (TAG_CATEGORY.equals(tag)) {
            return "分类";
        }
        if (TAG_FAVORITE.equals(tag)) {
            return "收藏";
        }
        if (TAG_HISTORY.equals(tag)) {
            return "历史";
        }
        return "首页";
    }

    private int getMenuItemIdForTag(String tag) {
        if (TAG_CATEGORY.equals(tag)) {
            return R.id.menu_category;
        }
        if (TAG_FAVORITE.equals(tag)) {
            return R.id.menu_favorite;
        }
        if (TAG_HISTORY.equals(tag)) {
            return R.id.menu_history;
        }
        return R.id.menu_home;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        if (itemId == R.id.menu_logout) {
            sessionManager.logout();
            goLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
