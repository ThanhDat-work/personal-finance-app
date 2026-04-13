package com.example.quanlychitieu.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlychitieu.MainActivity;
import com.example.quanlychitieu.R;

public class LoginActivity extends AppCompatActivity {

    EditText edtPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = edtPassword.getText().toString();

                // Kiểm tra mật khẩu
                if (pass.equals("171002")) {
                    // Nếu đúng, chuyển sang trang chủ
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Đóng luôn trang login để không quay lại được bằng nút Back
                } else {
                    Toast.makeText(LoginActivity.this, "Sai mật khẩu rồi!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}