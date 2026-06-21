package com.compuspulse.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.compuspulse.R
import com.compuspulse.data.repository.AuthRepository
import com.compuspulse.ui.main.OptimizedMainActivity
import com.compuspulse.utils.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authRepository = AuthRepository()
        sessionManager = SessionManager(this)

        etUsername = findViewById(R.id.et_username_register)
        etPassword = findViewById(R.id.et_password_register)
        etConfirmPassword = findViewById(R.id.et_confirm_password_register)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirm = etConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirm) {
            Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            authRepository.register(username, password)
                .onSuccess {
                    Toast.makeText(this@RegisterActivity, "注册成功", Toast.LENGTH_SHORT).show()
                    sessionManager.saveLogin(username)
                    val intent = Intent(this@RegisterActivity, OptimizedMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .onFailure { throwable ->
                    Toast.makeText(
                        this@RegisterActivity,
                        throwable.message ?: "注册失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
