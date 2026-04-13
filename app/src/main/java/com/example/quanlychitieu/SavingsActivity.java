package com.example.quanlychitieu;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class SavingsActivity extends AppCompatActivity {

    EditText edtAmount, edtDate, edtContent;
    Spinner spnService;
    Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Mặc áo của trang Tiết kiệm
        setContentView(R.layout.activity_savings);

        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtContent = findViewById(R.id.edtContent);
        spnService = findViewById(R.id.spnService);
        btnConfirm = findViewById(R.id.btnConfirm);

        // 2. Danh sách loại tiết kiệm
        String[] services = {"Quỹ mổ mắt", "Quỹ cá nhân"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, services);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spnService.setAdapter(adapter);

        // 3. Chọn ngày
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                // 1. Set ngày vào Calendar
                c.set(year, month, day);

                // 2. Dùng SimpleDateFormat để ép kiểu dd/MM/yyyy
                // "dd" là 2 số ngày, "MM" là 2 số tháng (M viết hoa)
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

                // 3. Hiển thị lên EditText
                edtDate.setText(sdf.format(c.getTime()));

            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 4. Xác nhận
        btnConfirm.setOnClickListener(v -> {
            String amount = edtAmount.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String content = edtContent.getText().toString().trim();

            // Kiểm tra xem có ô nào trống không
            if (amount.isEmpty() || date.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                // Nếu đủ thông tin mới hiện Dialog xác nhận
                showConfirmDialog();
            }
        });
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận tiết kiệm")
                .setMessage("Bạn muốn thêm khoản này vào quỹ tiết kiệm?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    saveToXML(); // Tiết kiệm chỉ có cộng nên không cần truyền tham số type
                    finish();
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void saveToXML() {
        SharedPreferences pref = getSharedPreferences("UserDailyData", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        long amountValue = Long.parseLong(edtAmount.getText().toString().trim());

        // THUẬT TOÁN CHO TIẾT KIỆM
        // 1. Đọc số dư tiết kiệm hiện tại (mặc định 7tr như bạn setup ban đầu)
        long currentSavings = pref.getLong("total_savings", 7315000);
        long newSavings = currentSavings + amountValue;

        // 2. Lưu lại số dư tiết kiệm mới
        editor.putLong("total_savings", newSavings);

        // 3. Lưu lịch sử với ký hiệu "*"
        String recordId = "REC_" + System.currentTimeMillis();
        String recordData = edtDate.getText().toString() + "|" +
                edtAmount.getText().toString() + "|" +
                spnService.getSelectedItem().toString() + "|" +
                edtContent.getText().toString() + "|*";

        editor.putString(recordId, recordData);

        // Cập nhật danh sách ID để quản lý
        String oldIds = pref.getString("all_record_ids", "");
        editor.putString("all_record_ids", oldIds + recordId + ",");

        editor.apply();
        Toast.makeText(this, "Đã thêm vào quỹ tiết kiệm!", Toast.LENGTH_SHORT).show();
    }
}