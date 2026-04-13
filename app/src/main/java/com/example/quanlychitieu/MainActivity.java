package com.example.quanlychitieu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color; // Thêm cái này để đổi màu chữ nợ
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlychitieu.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    TextView txtWelcome, txtBalance, txtSavings, txtDebt;
    LinearLayout btnDeposit, btnWithdraw, btnSavings, btnHistory, btnStats, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ toàn bộ View
        txtWelcome = findViewById(R.id.txtWelcome);
        txtBalance = findViewById(R.id.txtBalance);
        txtSavings = findViewById(R.id.txtSavings);
        txtDebt = findViewById(R.id.txtDebt);

        btnDeposit = findViewById(R.id.btnDeposit);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        btnSavings = findViewById(R.id.btnSavings);
        btnHistory = findViewById(R.id.btnHistory);
        btnStats = findViewById(R.id.btnStats);
        btnExit = findViewById(R.id.btnExit);

        // 2. Thiết lập thông tin cá nhân
        txtWelcome.setText("Chào, Đạt!");
        //resetAppToDefault();
        // 3. Cập nhật dữ liệu từ file XML (Lần đầu khi mở app)
        updateBalanceUI();

        // 4. Các sự kiện Click
        btnDeposit.setOnClickListener(v -> startActivity(new Intent(this, DepositActivity.class)));
        btnWithdraw.setOnClickListener(v -> startActivity(new Intent(this, WithDrawActivity.class)));
        btnSavings.setOnClickListener(v -> startActivity(new Intent(this, SavingsActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);

            // Dòng này cực kỳ quan trọng: Nó sẽ xóa sạch các Activity khác đang chạy ngầm
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // Kết thúc trang hiện tại
            finish();
        });
    }

    // ĐÂY LÀ HÀM QUAN TRỌNG NHẤT ĐỂ ĐỒNG BỘ
    private void updateBalanceUI() {
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);

        // 1. Lấy số dư ví
        long currentBalance = pref.getLong("total_balance", 385000);
        txtBalance.setText(String.format("%,d VND", currentBalance));

        // 2. Lấy số dư tiết kiệm
        long currentSavings = pref.getLong("total_savings", 7315000);
        txtSavings.setText(String.format("%,d VND", currentSavings));

        // 3. Lấy số tiền nợ (MỚI CẬP NHẬT)
        long currentDebt = pref.getLong("total_debt", 0);
        txtDebt.setText(String.format("%,d VND", currentDebt));

        // Nếu có nợ thì cho chữ màu đỏ cho dễ chú ý
        if (currentDebt > 0) {
            txtDebt.setTextColor(Color.RED);
        } else {
            txtDebt.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh nếu không nợ
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại từ trang khác, updateBalanceUI sẽ chạy lại để lấy số liệu mới nhất
        updateBalanceUI();
    }
    
    // Hàm reset cũng cần xóa cả nợ
    private void resetAppToDefault() {
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putLong("total_balance", 385000);
        editor.putLong("total_savings", 7315000);
        editor.putLong("total_debt", 0); // Reset nợ về 0
        editor.putString("all_record_ids", "");
        editor.apply();
        updateBalanceUI();
        Toast.makeText(this, "Đã reset dữ liệu về ban đầu!", Toast.LENGTH_SHORT).show();
    }
}