package com.compuspulse.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.compuspulse.R;
import com.compuspulse.data.repository.AuthRepository;
import com.compuspulse.ui.main.OptimizedMainActivity;
import com.compuspulse.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLogin()) {
            goMain();
            return;
        }

        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository();

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoRegister = findViewById(R.id.tv_go_register);

        btnLogin.setOnClickListener(v -> doLogin());

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        authRepository.login(username, password, (success, message) -> runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            if (success) {
                sessionManager.saveLogin(username);
                goMain();
            }
        }));
    }

    private void goMain() {
        Intent intent = new Intent(this, OptimizedMainActivity.class);
        startActivity(intent);
        finish();
    }
}
