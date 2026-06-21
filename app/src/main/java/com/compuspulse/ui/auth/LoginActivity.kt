package com.compuspulse.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.compuspulse.R
import com.compuspulse.data.remote.http.NetworkClient
import com.compuspulse.data.repository.ArticleLocalRepository
import com.compuspulse.data.repository.AuthRepository
import com.compuspulse.ui.main.OptimizedMainActivity
import com.compuspulse.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoRegister: TextView

    private lateinit var authRepository: AuthRepository
    private lateinit var articleLocalRepository: ArticleLocalRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        authRepository = AuthRepository()
        articleLocalRepository = ArticleLocalRepository()

        val networkClient = NetworkClient.getInstance()
        val isLogin = sessionManager.isLogin()
        val hasCookies = networkClient.hasSessionCookies()
        if (isLogin && hasCookies) {
            goMain()
            return
        }
        if (isLogin || hasCookies) {
            clearInvalidLocalSession(networkClient)
        }

        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvGoRegister = findViewById(R.id.tv_go_register)

        btnLogin.setOnClickListener { doLogin() }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun clearInvalidLocalSession(networkClient: NetworkClient) {
        val username = sessionManager.getCurrentUsername()
        networkClient.clearCookies()
        sessionManager.logout()
        lifecycleScope.launch {
            articleLocalRepository.clearUserLocalData(username)
        }
    }

    private fun doLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            authRepository.login(username, password)
                .onSuccess {
                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    sessionManager.saveLogin(username)
                    goMain()
                }
                .onFailure { throwable ->
                    Toast.makeText(
                        this@LoginActivity,
                        throwable.message ?: "登录失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun goMain() {
        val intent = Intent(this, OptimizedMainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
